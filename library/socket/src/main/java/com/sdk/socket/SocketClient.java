package com.sdk.socket;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author lenovo
 * @date 2020/6/22
 */
public class SocketClient {
    private Socket socket;
    private SocketListener mListener;
    private Handler handler;
    private Object lock = new Object();
    private String ip;
    private int port = -1;
    private int retryNum = 0;
    private int retryCount = 0;

    private static class SingleTonHolder {
        private static final SocketClient INSTANCE = new SocketClient();
    }

    public static SocketClient getInstance() {
        return SingleTonHolder.INSTANCE;
    }

    private SocketClient() {
    }

    private ExecutorService executor = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.SECONDS, new LinkedTransferQueue<Runnable>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("socket-client");
            return t;
        }
    });

    public SocketClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void setSocketInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void setRetryNum(int num) {
        this.retryNum = num;
    }

    public boolean connect() {
        if (!isSocketClosed()) {
            return true;
        }

        if (TextUtils.isEmpty(ip) || port == -1) {
            return false;
        }

        final CountDownLatch latch = new CountDownLatch(1);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(ip, port);
                    socket.setSoTimeout(3000);
                } catch (IOException e) {
                }
                latch.countDown();
            }
        });
        try {
            latch.await(4, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        return !isSocketClosed();
    }

    /**
     * 开始监听,长连接，未启用
     */
    private void startListener() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (socket != null && !socket.isClosed()) {
                            InputStream stream = socket.getInputStream();
                            String message = DataReader.readToEnd(stream, "utf-8");
                            if (mListener != null) {
                                mListener.onReceive(message);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

    public SocketListener getListener() {
        return mListener;
    }

    public void setListener(SocketListener listener) {
        this.mListener = listener;
    }

    public boolean isSocketClosed() {
        return socket == null || socket.isClosed();
    }

    /**
     * 根据msgData获取返回值
     *
     * @param msg
     * @return
     */
    public MsgData getServerData(final MsgData msg) {
        if (!connect()) {
            return msg;
        }

        final AtomicBoolean hasException = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DataOutputStream output = null;
                InputStream stream = null;
                try {
                    output = new DataOutputStream(socket.getOutputStream());
                    msg.writeShortTo(output);
                    socket.shutdownOutput();

                    String message = null;
                    while (message == null) {
                        stream = socket.getInputStream();
                        message = DataReader.readToEndWithOutClose(stream, "utf-8");

                        if (message == null) {
                            continue;
                        }

                        try {
                            JSONObject object = new JSONObject(message);
                            MsgData res = new MsgData();
                            res.getFromJson(object);
                            msg.setResponse(res.getResponse());
                        } catch (JSONException e) {
                        }
                    }
                    retryCount = 0;
                    hasException.compareAndSet(true, false);
                } catch (IOException e) {
                    if (e != null && e instanceof SocketException) {
                        hasException.compareAndSet(false, true);
                    }
                }finally {
                    DataReader.close(stream);
                }
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        if (hasException.get() && retryCount < retryNum) {
            retryCount++;
            close();
            return getServerData(msg);
        }

        return msg;
    }

    /**
     * 写入数据
     *
     * @param msg
     */
    public void write(final MsgData msg) {
        if (!connect()) {
            return;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                PrintWriter pw = null;
                InputStream stream = null;
                try {
                    pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    pw.print(msg.toJson());
                    pw.flush();
                    socket.shutdownOutput();

                    stream = socket.getInputStream();
                    String message = DataReader.readWithDataStream(stream, "utf-8");
                    if (mListener != null && !TextUtils.isEmpty(message)) {
                        mListener.onReceive(message);
                    }
                } catch (IOException e) {
                } finally {
//                    DataReader.close(stream);
//                    DataReader.close(pw);
//                    close();
                }
            }
        });
    }

    /**
     * 关闭连接
     */
    public void close() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
        }
    }

    public interface SocketListener {
        void onReceive(String message);
    }
}
