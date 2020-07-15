package com.sdk.socket;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author lenovo
 * @date 2020/6/23
 */
public class MsgData {
    public static final int MESSAGE_REQUEST_URL = 1;
    public static final int MESSAGE_REQUEST_DEBUG = 2;
    public static final int MESSAGE_REQUEST_LOG = 3;
    public static final int MESSAGE_REQUEST_OTHER = 4;

    private int code;
    private String request;
    private String response;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
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
            request = object.optString("request", "");
            response = object.optString("response", "");
        }
    }
}
