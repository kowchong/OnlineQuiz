package com.example.onlinequiz.model;

/**
 * Created by Administrator on 21/5/2559.
 */
public class ResponseStatus {
    public final boolean success;
    public final String message;


    public ResponseStatus(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
