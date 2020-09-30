package com.sdk.socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.sdk.socket.MsgData.CODE_REQUEST_DATA;
import static com.sdk.socket.MsgData.CODE_REQUEST_DEBUG;
import static com.sdk.socket.MsgData.CODE_REQUEST_LOG;
import static com.sdk.socket.MsgData.CODE_REQUEST_OTHER;

/**
 * @author lenovo
 * @date 2020/6/22
 */
public class SocketServer {
    private static final int PORT = 10110;
    private static final int POOL_SIZE = 8;
    private ServerSocket server;
    private ServerHandler serverHandler;
    private ExecutorService executorService;
    private SocketServerListener mListener;
    private int port = -1;
    private AtomicBoolean isServerClosed = new AtomicBoolean(false);

    public SocketServer(int port) {
        this.port = port;
    }

    public int getPort() {
        if (port == -1) {
            return PORT;
        }
        return port;
    }

    /**
     * 初始化
     */
    public void init() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(POOL_SIZE, Integer.MAX_VALUE, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("socket-server");
                    return t;
                }
            });
        }
        if (serverHandler == null) {
            serverHandler = new ServerHandler(SocketServer.this, Looper.getMainLooper());
        }
    }

    public SocketServerListener getListener() {
        return mListener;
    }

    public void setListener(SocketServerListener l) {
        this.mListener = l;
    }

    /**
     * 创建socket
     */
    private void createSocket() {
        try {
            if (server == null || server.isClosed()) {
                server = new ServerSocket(getPort());
            }
            isServerClosed.compareAndSet(true, false);
        } catch (IOException e) {
        }
    }

    /**
     * 开始监听socket
     */
    public void startSocket() {
        createSocket();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                ServerSocket ss = server;
                while (!isServerClosed.get()) {
                    try {
                        Socket socket = ss.accept();
                        executorService.execute(new ServerSocketRunnable(SocketServer.this, socket, serverHandler));
                    } catch (Exception e) {
                        DataReader.close(ss);
                        if (!isServerClosed.get()) {
                            createSocket();
                            ss = server;
                        }
                    }
                }
            }
        });
    }

    private static class ServerHandler extends Handler {

        private WeakReference<SocketServer> mReference;

        public ServerHandler(SocketServer service, Looper looper) {
            super(looper);
            mReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_REQUEST_DATA:
                case CODE_REQUEST_DEBUG:
                case CODE_REQUEST_LOG:
                    MsgObject mobject = (MsgObject) msg.obj;
                    handleMsgObject(mobject);
                    break;
                default:
                    MsgObject o_o = (MsgObject) msg.obj;
                    handleMsgObject(o_o);
                    super.handleMessage(msg);
                    break;
            }
        }

        /**
         * 处理handle
         *
         * @param o
         */
        private void handleMsgObject(MsgObject o) {
            if (mReference != null && mReference.get() != null && mReference.get().getListener() != null) {
                mReference.get().getListener().onReceive(o);
            }
        }
    }

    /**
     * 写入数据到client
     *
     * @param mobject
     */
    public void writeDataToClient(final MsgObject mobject) {
        final Socket socket = mobject.socket;
        final CountDownLatch count = new CountDownLatch(1);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                DataOutputStream os = null;
                try {
                    MsgData data = mobject.data;
                    os = new DataOutputStream(socket.getOutputStream());
                    data.writeShortTo(os);
                } catch (IOException e) {
                } finally {
                    DataReader.close(os);
                    DataReader.close(socket);
                    count.countDown();
                }
            }
        });
        try {
            count.await();
        } catch (InterruptedException e) {
        }
    }

    private static class ServerSocketRunnable implements Runnable {
        private Socket socket;
        private Handler handler;
        private WeakReference<SocketServer> server;

        public ServerSocketRunnable(SocketServer server, Socket socket, Handler handler) {
            this.server = new WeakReference<>(server);
            this.socket = socket;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                while (socket != null && !socket.isClosed()) {
//                    String message = DataReader.readWithDataStream(socket.getInputStream(), "UTF-8");
                    String message = DataReader.readToEndWithOutClose(socket.getInputStream(), "UTF-8");
                    handleClient(message);
                }
            } catch (Exception e) {
            }
        }

        private void handleClient(String message) {
            if (TextUtils.isEmpty(message)) {
                return;
            }

            try {
                JSONObject json = new JSONObject(message);
                MsgData msgData = new MsgData();
                msgData.getFromJson(json);
                if (msgData != null && handler != null) {
                    Message msg = null;
                    switch (msgData.getCode()) {
                        case CODE_REQUEST_DATA:
                            msg = Message.obtain(handler, CODE_REQUEST_DATA, new MsgObject(msgData, socket));
                            break;
                        case CODE_REQUEST_DEBUG:
                            msg = Message.obtain(handler, CODE_REQUEST_DEBUG, new MsgObject(msgData, socket));
                            break;
                        case CODE_REQUEST_LOG:
                            msg = Message.obtain(handler, CODE_REQUEST_LOG, new MsgObject(msgData, socket));
                            break;
                        default:
                            msg = Message.obtain(handler, CODE_REQUEST_OTHER, new MsgObject(msgData, socket));
                            break;
                    }
                    if (msg != null) {
                        handler.handleMessage(msg);
                    } else if (server.get() != null) {
                        server.get().writeDataToClient(new MsgObject(msgData, socket));
                    }
                }
            } catch (JSONException e) {
                handleFromOtherType(message);
            }
        }

        /**
         * 处理其他情况(非json数据)
         * 默认code为-100
         *
         * @param message
         */
        private void handleFromOtherType(String message) {
            if (server != null && server.get() != null && server.get().getListener() != null) {
                MsgData data = new MsgData();
                data.setCode(-100);
                data.setRequest(message);
                MsgObject object = new MsgObject(data, socket);
                server.get().getListener().onReceive(object);
            }
        }
    }


    public static class MsgObject {
        public MsgData data;
        public Socket socket;

        public MsgObject(MsgData data, Socket socket) {
            this.data = data;
            this.socket = socket;
        }
    }

    public void close() {
        try {
            isServerClosed.compareAndSet(false, true);
            if (server != null) {
                server.close();
            }
        } catch (IOException e) {
        }
    }

    public interface SocketServerListener {
        /**
         * 返回接口
         *
         * @param data
         */
        void onReceive(MsgObject object);
    }

}
