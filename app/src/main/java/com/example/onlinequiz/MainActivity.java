package com.example.onlinequiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.onlinequiz.model.ResponseStatus;
import com.example.onlinequiz.model.User;
import com.example.onlinequiz.net.WebServices;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_REGISTER = 123;
    protected static final String USERNAME_INTENT_KEY = "username";

    private static enum ScreenType {
        MAIN, LOGIN, SHOW_WAIT, HIDE_WAIT
    }

    private View mMainScreen, mLoginScreen, mWaitScreen;
    private Button mLoginButton;
    private TextView mRegisterTextView;
    private EditText mUsernameEditText, mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (User.loggedInUser != null){
            Intent intent = new Intent(MainActivity.this, QuizListActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        mMainScreen = findViewById(R.id.main_screen);
        mLoginScreen = findViewById(R.id.login_screen);
        mWaitScreen = findViewById(R.id.wait_screen);

        mLoginButton = (Button) findViewById(R.id.login_button);
        mRegisterTextView = (TextView) findViewById(R.id.register_text);

        mUsernameEditText = (EditText) findViewById(R.id.username);
        mPasswordEditText = (EditText) findViewById(R.id.password);

        mLoginButton.setOnClickListener(this);
        mRegisterTextView.setOnClickListener(this);

        SpannableString text = new SpannableString("New user? Register here.");
        text.setSpan(new UnderlineSpan(), text.toString().indexOf("Register"), text.length(), 0);
        mRegisterTextView.setText(text);

        updateUI();


    }


    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.login_button:
                String username = mUsernameEditText.getText().toString().trim();
                String password = mPasswordEditText.getText().toString().trim();
                login(username, password);
                break;
            case R.id.register_text:
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivityForResult(intent, REQUEST_CODE_REGISTER);
                break;
        }


    }

    private void updateUI() {
        if (User.loggedInUser != null) {
            setScreen(ScreenType.MAIN);
        } else {
            setScreen(ScreenType.LOGIN);
        }
    }

    private void setScreen(ScreenType type) {
        if (type == ScreenType.MAIN) {
            mMainScreen.setVisibility(View.VISIBLE);
            mLoginScreen.setVisibility(View.GONE);
        } else if (type == ScreenType.LOGIN) {
            mMainScreen.setVisibility(View.GONE);
            mLoginScreen.setVisibility(View.VISIBLE);
        } else if (type == ScreenType.SHOW_WAIT) {
            mWaitScreen.setVisibility(View.VISIBLE);
        } else if (type == ScreenType.HIDE_WAIT) {
            mWaitScreen.setVisibility(View.GONE);
        }
    }

    private void login(String username, String password) {
        setScreen(ScreenType.SHOW_WAIT);

        WebServices.login(username, password, new WebServices.LoginCallback() {
            @Override
            public void onFailure(IOException e) {
                setScreen(ScreenType.HIDE_WAIT);
                showModalOkDialog("Error", "Unable to connect to server.");
            }

            @Override
            public void onResponse(ResponseStatus responseStatus, User user) {
                setScreen(ScreenType.HIDE_WAIT);
                if (user != null) {
                    //login succeed
                    User.loggedInUser = user;
                    //updateUI();
                    Intent intent = new Intent(MainActivity.this, QuizListActivity.class);
                    startActivity(intent);
                    finish();


                } else {
                    showModalOkDialog("Login Failed", "Invalid username or password.");
                }
            }
        });
    }

    private void showModalOkDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_REGISTER) {
            if (resultCode == RESULT_OK) {
                String username = data.getStringExtra(USERNAME_INTENT_KEY);
                mUsernameEditText.setText(username);
            }
        }


    }
}
