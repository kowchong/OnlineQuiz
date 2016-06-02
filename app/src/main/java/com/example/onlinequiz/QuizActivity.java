package com.example.onlinequiz;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.onlinequiz.model.LoadDataCallback;
import com.example.onlinequiz.model.Question;
import com.example.onlinequiz.model.Questions;
import com.example.onlinequiz.model.ResponseStatus;
import com.example.onlinequiz.net.WebServices;

import java.io.IOException;
import java.util.ArrayList;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = QuizActivity.class.getSimpleName();
    protected static final String KEY_EXTRA_QUIZ_ID = "quiz_id";

    private QuestionsPagerAdapter mAdapter;
    private TabLayout mTabLayout;
    private ProgressBar mProgressBar;
    private ViewPager mViewPager;
    private FloatingActionButton mFab;

    private int mQuizId;
    private Questions mQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /* *************************************************************
         * ต้องสร้าง instance ของ Questions ก่อนเรียก super.onCreate
         * เพราะ super.onCreate จะมีการ restore แฟรกเมนต์ กรณี config change
         * และในแฟรกเมนต์ เราจะอ่านข้อมูลจาก mQuestions
         * *************************************************************/

        Intent intent = getIntent();
        mQuizId = intent.getIntExtra(KEY_EXTRA_QUIZ_ID, 0);
        mQuestions = Questions.getInstance(mQuizId);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        setupViews();
        loadQuestions();
    }

    private void setupViews() {
        mViewPager = (ViewPager) findViewById(R.id.container);
        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        final View coordinatorLayout = findViewById(R.id.main_content);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setVisibility(View.GONE);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isQuizComplete()) {
                    new AlertDialog.Builder(QuizActivity.this)
                            .setTitle("ต้องการส่งข้อมูล?")
                            .setPositiveButton("ส่ง", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    WebServices.setUserGuesses(1, mQuizId, mQuestions.getList(),
                                            new WebServices.SetUserGuessesCallback() {
                                                @Override
                                                public void onFailure(IOException e) {
                                                    String msg = "Network Connection Error:\n" + e.getMessage();
                                                    Log.e(TAG, msg);
                                                    Snackbar.make(
                                                            coordinatorLayout,
                                                            msg,
                                                            Snackbar.LENGTH_LONG
                                                    ).show();
                                                }

                                                @Override
                                                public void onResponse(ResponseStatus responseStatus) {
                                                    if (responseStatus.success) {
                                                        Toast.makeText(
                                                                QuizActivity.this,
                                                                responseStatus.message,
                                                                Toast.LENGTH_LONG
                                                        ).show();

                                                        finish();
                                                    } else {
                                                        Snackbar.make(
                                                                coordinatorLayout,
                                                                responseStatus.message,
                                                                Snackbar.LENGTH_LONG
                                                        ).show();
                                                    }
                                                }
                                            });
                                }
                            })
                            .setNegativeButton("ยกเลิก", null)
                            .show();

                } else {
                    Snackbar.make(coordinatorLayout, "คุณยังทำแบบทดสอบไม่ครบทุกข้อ", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loadQuestions() {
        mProgressBar.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.GONE);

        mQuestions.load(mQuizId, new LoadDataCallback() {
            @Override
            public void onFailure(String errMessage) {
                mProgressBar.setVisibility(View.GONE);
                mViewPager.setVisibility(View.GONE);

                Log.e(TAG, errMessage);
                Toast.makeText(QuizActivity.this, errMessage, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess() {
                mProgressBar.setVisibility(View.GONE);
                mViewPager.setVisibility(View.VISIBLE);

                mAdapter = new QuestionsPagerAdapter(getSupportFragmentManager());
                mViewPager.setAdapter(mAdapter);
                mTabLayout.setupWithViewPager(mViewPager);
            }
        });
    }

    protected void checkQuizComplete() {
        if (isQuizComplete()) {
            mFab.setVisibility(View.VISIBLE);
        } else {
            mFab.setVisibility(View.GONE);
        }
    }

    private boolean isQuizComplete() {
        for (Question question : Questions.getInstance(mQuizId).getList()) {
            if (question.getSelectedChoiceId() == Question.NO_CHOICE_SELECTED) {
                return false;
            }
        }
        return true;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_quiz, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    public ArrayList<Question> getQuestionList() {
        return mQuestions.getList();
    }

    public class QuestionsPagerAdapter extends FragmentPagerAdapter {

        public QuestionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return QuestionFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return mQuestions.getList().size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return String.valueOf(position + 1);
        }
    }
}
