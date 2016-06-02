package com.example.onlinequiz.model;

/**
 * Created by Administrator on 22/5/2559.
 */
public interface LoadDataCallback {

    void onFailure(String errMessage);
    void onSuccess();

}
