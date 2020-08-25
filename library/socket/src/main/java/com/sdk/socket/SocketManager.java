package com.sdk.socket;

public class SocketManager {

    private static class SingleTonHolder {
        private static final SocketManager INSTANCE = new SocketManager();
    }

    public static SocketManager getInstance() {
        return SingleTonHolder.INSTANCE;
    }

    private SocketManager() {
    }

    private SocketClient createSocket() {
        SocketClient client = SocketClient.getInstance();
        client.setSocketInfo("127.0.0.1", 10110);
        client.setRetryNum(1);
        client.connect();
        return client;
    }

    public String getData(MsgData data) {
        return getSocketData(data);
    }

    public void setData(MsgData data) {
        getSocketData(data);
    }

    public String getURLManager() {
        MsgData data = new MsgData();
        data.setCode(MsgData.CODE_REQUEST_DATA);
        data.setType(MsgData.TYPE_REQUEST_URLMANAGER);
        return getSocketData(data);
    }

    public String getDebugInfo() {
        MsgData data = new MsgData();
        data.setCode(MsgData.CODE_REQUEST_DEBUG);
        return getSocketData(data);
    }

    public String getLog() {
        MsgData data = new MsgData();
        data.setCode(MsgData.CODE_REQUEST_LOG);
        return getSocketData(data);
    }

    private String getSocketData(MsgData data) {
        SocketClient client = createSocket();
        MsgData response = client.getServerData(data);
        if (response == null) {
            return "";
        } else {
            return response.getResponse();
        }
    }

    /**
     * 关闭服务
     */
    public void close() {
        SocketClient.getInstance().close();
    }

}
