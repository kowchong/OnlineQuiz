package com.example.onlinequiz;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.onlinequiz.model.Choice;
import com.example.onlinequiz.model.Question;

import java.util.ArrayList;

/**
 * Created by Promlert on 1/14/2016.
 */
public class QuestionFragment extends Fragment {

    private static final String TAG = QuestionFragment.class.getSimpleName();
    private static final String ARG_QUESTION_ITEM_POSITION = "question_item_position";

    private ArrayList<Question> mQuestionList;
    private int mQuestionItemPosition;

    public QuestionFragment() {
    }

    public static QuestionFragment newInstance(int questionItemPosition) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_QUESTION_ITEM_POSITION, questionItemPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mQuestionList = ((QuizActivity) getActivity()).getQuestionList();
        Bundle args = getArguments();
        mQuestionItemPosition = args.getInt(ARG_QUESTION_ITEM_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_question, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView questionTitleTextView = (TextView) view.findViewById(R.id.question_title_text_view);
        ImageView questionPictureImageView = (ImageView) view.findViewById(R.id.question_picture_image_view);
        TextView questionDetailTextView = (TextView) view.findViewById(R.id.question_detail_text_view);
        RadioGroup choicesRadioGroup = (RadioGroup) view.findViewById(R.id.choices_radio_group);

        final Question question = mQuestionList.get(mQuestionItemPosition);

        questionTitleTextView.setText(question.title);
        questionDetailTextView.setText(question.detail);

        if (question.picture == null) {
            questionPictureImageView.setVisibility(View.GONE);
        } else {
            questionPictureImageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(question.picture).into(questionPictureImageView);
        }

        LayoutInflater inflater = getLayoutInflater(null);

        for (Choice choice : question.choiceArrayList) {
            RadioButton choiceRadioButton = (RadioButton)
                    inflater.inflate(R.layout.choice_button, choicesRadioGroup, false);

            choiceRadioButton.setId(choice.choiceId);
            choiceRadioButton.setText(choice.text);

            choicesRadioGroup.addView(choiceRadioButton);
        }

        choicesRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Toast.makeText(getActivity(), "Choice ID: " + checkedId, Toast.LENGTH_SHORT).show();

                question.setSelectedChoiceId(checkedId);
                ((QuizActivity) getActivity()).checkQuizComplete();
            }
        });
    }
}
