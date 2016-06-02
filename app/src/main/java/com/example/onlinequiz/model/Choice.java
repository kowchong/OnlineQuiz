package com.example.onlinequiz.model;

/**
 * Created by Promlert on 1/14/2016.
 */
public class Choice {

    public final int choiceId;
    public final String text;
    public final boolean isAnswer;

    public Choice(int choiceId, String text, boolean isAnswer) {
        this.choiceId = choiceId;
        this.text = text;
        this.isAnswer = isAnswer;
    }
}
