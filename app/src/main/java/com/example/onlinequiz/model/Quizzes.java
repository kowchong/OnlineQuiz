package com.example.onlinequiz.model;

import com.example.onlinequiz.net.WebServices;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Promlert on 3/23/2016.
 */
public class Quizzes {

    private static Quizzes mInstance;
    private ArrayList<Quiz> mQuizArrayList;

    private Quizzes() {
    }

    public static Quizzes getInstance() {
        if (mInstance == null) {
            mInstance = new Quizzes();
        }
        return mInstance;
    }

    public void load(final LoadDataCallback callback) {
        if (mQuizArrayList != null) {
            callback.onSuccess();
        } else {
            WebServices.getQuizzes(new WebServices.GetQuizzesCallback() {
                @Override
                public void onFailure(IOException e) {
                    String msg = "Network Connection Error:\n" + e.getMessage();
                    callback.onFailure(msg);
                }

                @Override
                public void onResponse(ResponseStatus responseStatus, ArrayList<Quiz> quizArrayList) {
                    if (responseStatus.success) {
                        mQuizArrayList = quizArrayList;
                        callback.onSuccess();
                    } else {
                        callback.onFailure(responseStatus.message);
                    }
                }
            });
        }
    }

    public void clear() {
        mQuizArrayList = null;
    }

    public ArrayList<Quiz> getList() {
        return mQuizArrayList;
    }
}
