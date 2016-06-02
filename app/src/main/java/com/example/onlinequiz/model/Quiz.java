package com.example.onlinequiz.model;

/**
 * Created by Administrator on 22/5/2559.
 */
public class Quiz {

    public final int quizId;
    public final String title;
    public final String detail;
    public final int numberOfQuestions;

    public Quiz(int quizId, String title, String detail, int numberOfQuestions) {
        this.quizId = quizId;
        this.title = title;
        this.detail = detail;
        this.numberOfQuestions = numberOfQuestions;
    }

    @Override
    public String toString() {
        return title;
    }
}
