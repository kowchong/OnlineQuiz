package com.example.onlinequiz.model;

import com.example.onlinequiz.net.WebServices;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Promlert on 3/23/2016.
 */
public class Questions {

    private static Questions mInstance;
    private ArrayList<Question> mQuestionArrayList;
    private int mQuizId;

    private Questions() {
    }

    public static Questions getInstance(int quizId) {
        if (mInstance == null || mInstance.mQuizId != quizId) {
            mInstance = new Questions();
            mInstance.mQuizId = quizId;
        }
        return mInstance;
    }

    public void load(int quizId, final LoadDataCallback callback) {
        if (mQuestionArrayList != null) {
            callback.onSuccess();
        } else {
            WebServices.getQuestions(quizId, new WebServices.GetQuestionsCallback() {
                @Override
                public void onFailure(IOException e) {
                    String msg = "Network Connection Error:\n" + e.getMessage();
                    callback.onFailure(msg);
                }

                @Override
                public void onResponse(ResponseStatus responseStatus, ArrayList<Question> questionArrayList) {
                    if (responseStatus.success) {
                        mQuestionArrayList = questionArrayList;
                        callback.onSuccess();
                    } else {
                        callback.onFailure(responseStatus.message);
                    }
                }
            });
        }
    }

    public void clear() {
        mQuestionArrayList = null;
    }

    public ArrayList<Question> getList() {
        return mQuestionArrayList;
    }
}
