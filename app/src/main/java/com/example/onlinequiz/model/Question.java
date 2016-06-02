package com.example.onlinequiz.model;

import java.util.ArrayList;

/**
 * Created by Promlert on 1/14/2016.
 */
public class Question {

    public static final int NO_CHOICE_SELECTED = -1;

    public final int questionId;
    public final String title;
    public final String detail;
    public final String picture;
    private int mSelectedChoiceId = NO_CHOICE_SELECTED;

    public final ArrayList<Choice> choiceArrayList = new ArrayList<>();

    public Question(int questionId, String title, String detail, String picture) {
        this.questionId = questionId;
        this.title = title;
        this.detail = detail;
        this.picture = picture;
    }

    public void setSelectedChoiceId(int selectedChoiceId) {
        this.mSelectedChoiceId = selectedChoiceId;
    }

    public int getSelectedChoiceId() {
        return mSelectedChoiceId;
    }
}
