package com.example.onlinequiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.onlinequiz.model.LoadDataCallback;
import com.example.onlinequiz.model.Quiz;
import com.example.onlinequiz.model.Quizzes;
import com.example.onlinequiz.model.User;
import com.example.onlinequiz.net.WebServices;

import java.util.ArrayList;

public class QuizListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = QuizListActivity.class.getSimpleName();

    private View mMainLayout;
    private ListView mQuizzesListView;
    private ProgressBar mProgressBar;
    private View mRetryLayout;

    private Quizzes mQuizzes = Quizzes.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        ImageView profileImageView = (ImageView) header.findViewById(R.id.profile_image_view);
        TextView nameTextView = (TextView) header.findViewById(R.id.name_text_view);
        TextView emailTextView = (TextView) header.findViewById(R.id.email_text_view);

        Glide.with(this)
                .load(WebServices.USER_IMAGE_URL + User.loggedInUser.picture)
                .into(profileImageView);

        nameTextView.setText(User.loggedInUser.name);
        emailTextView.setText(User.loggedInUser.username);

        setupViews();
        loadQuizzes();
    }

    private void setupViews() {
        mMainLayout = findViewById(R.id.drawer_layout);

        mQuizzesListView = (ListView) findViewById(R.id.quizzes_list_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mRetryLayout = findViewById(R.id.retry_layout);

        final ArrayAdapter<Quiz> adapter = new ArrayAdapter<>(
                QuizListActivity.this,
                R.layout.quiz_item,
                new ArrayList<Quiz>()
        );
        mQuizzesListView.setAdapter(adapter);
        mQuizzesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Quiz selectedQuiz = adapter.getItem(position);

                Toast.makeText(
                        QuizListActivity.this,
                        selectedQuiz.toString(),
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(QuizListActivity.this, QuizActivity.class);
                intent.putExtra(QuizActivity.KEY_EXTRA_QUIZ_ID, selectedQuiz.quizId);
                startActivity(intent);
            }
        });

        Button retryButton = (Button) findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadQuizzes();
            }
        });
    }

    private void loadQuizzes() {
        mMainLayout.setBackgroundResource(0); // remove background
        mProgressBar.setVisibility(View.VISIBLE);
        mQuizzesListView.setVisibility(View.GONE);
        mRetryLayout.setVisibility(View.GONE);

        mQuizzes.load(new LoadDataCallback() {
            @Override
            public void onFailure(String errMessage) {
                mMainLayout.setBackgroundResource(0); // remove background
                mProgressBar.setVisibility(View.GONE);
                mQuizzesListView.setVisibility(View.GONE);
                mRetryLayout.setVisibility(View.VISIBLE);

                TextView errorMessageTextView = (TextView) findViewById(R.id.error_message);
                errorMessageTextView.setText(errMessage);
            }

            @Override
            public void onSuccess() {
                mMainLayout.setBackgroundResource(R.drawable.background); // set background
                mProgressBar.setVisibility(View.GONE);
                mQuizzesListView.setVisibility(View.VISIBLE);
                mRetryLayout.setVisibility(View.GONE);

                ArrayAdapter<Quiz> adapter = (ArrayAdapter<Quiz>) mQuizzesListView.getAdapter();
                adapter.addAll(mQuizzes.getList());
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.quiz_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



}
