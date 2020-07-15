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
        SocketClient client = new SocketClient("127.0.0.1", 10110);
        client.connect();
        return client;
    }

    public String getURLManager() {
        MsgData data = new MsgData();
        data.setCode(1);
        data.setRequest("request url");
        return getSocketData(data);
    }

    public String getDebugInfo() {
        MsgData data = new MsgData();
        data.setCode(2);
        data.setRequest("request debug");
        return getSocketData(data);
    }

    public String getLog() {
        MsgData data = new MsgData();
        data.setCode(3);
        data.setRequest("request log");
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

}
