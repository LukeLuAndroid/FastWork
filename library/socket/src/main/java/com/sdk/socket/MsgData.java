package com.sdk.socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author lenovo
 * @date 2020/6/23
 */
public class MsgData {
    /**
     * 请求的code
     */
    public static final int CODE_REQUEST_DATA = 1;
    public static final int CODE_REQUEST_DEBUG = 2;
    public static final int CODE_REQUEST_LOG = 3;
    public static final int CODE_REQUEST_OTHER = 4;

    /**
     * 请求的type
     */
    public static final int TYPE_REQUEST_DSP_DEBUG = 0;
    public static final int TYPE_REQUEST_URLMANAGER = 1;
    public static final int TYPE_REQUEST_SDKLIST = 2;
    public static final int TYPE_REQUEST_PLUGINS = 3;

    /**
     * 表示code的最长值
     */
    public static final int CODE_TYPE_LENGTH = 10;

    private int code;
    private int type;
    private String request;
    private String response;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String toJson() {
        try {
            JSONObject object = new JSONObject();
            object.put("code", code);
            object.put("type", type);
            object.put("request", request);
            object.put("response", response);
            return object.toString();
        } catch (JSONException e) {
        }
        return "";
    }

    public void getFromJson(JSONObject object) {
        if (object != null) {
            code = object.optInt("code", -1);
            type = object.optInt("type", -1);
            request = object.optString("request", "");
            response = object.optString("response", "");
        }
    }

    public void writeTo(DataOutputStream output) throws IOException {
        byte[] data = toJson().getBytes();
        int len = data.length + 5;
        output.writeByte(getCode());
        output.writeInt(len);
        output.write(data);
        output.flush();
    }
}
