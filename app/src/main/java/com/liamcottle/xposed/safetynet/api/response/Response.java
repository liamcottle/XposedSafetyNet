package com.liamcottle.xposed.safetynet.api.response;

import com.google.gson.annotations.SerializedName;

public class Response {

    private static final String STATUS_OK = "ok";

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    public String getStatus(){
        return status;
    }

    public String getMessage(){
        return message;
    }

    public boolean isOk(){
        return STATUS_OK.equals(status);
    }

}
