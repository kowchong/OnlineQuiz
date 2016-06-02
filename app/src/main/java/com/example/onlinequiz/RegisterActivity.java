package com.example.onlinequiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.onlinequiz.model.ResponseStatus;
import com.example.onlinequiz.net.WebServices;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenImages;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.kbeanie.imagechooser.exceptions.ChooserException;

import java.io.IOException;

public class RegisterActivity extends AppCompatActivity implements ImageChooserListener {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private View mScreenWait;
    private ImageView mPictureImageView;
    private EditText mNameEditText, mEmailEditText, mPasswordEditText, mConfirmPasswordEditText;

    private ImageChooserManager mImageChooserManager;
    private ChosenImage mChosenImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mScreenWait = findViewById(R.id.screen_wait);
        mPictureImageView = (ImageView) findViewById(R.id.picture);

        mNameEditText = (EditText) findViewById(R.id.name);
        mEmailEditText = (EditText) findViewById(R.id.email);
        mPasswordEditText = (EditText) findViewById(R.id.password);
        mConfirmPasswordEditText = (EditText) findViewById(R.id.confirm_password);

        mPictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] items = {"Choose an image from gallery", "Take a photo"};

                new AlertDialog.Builder(RegisterActivity.this)
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: // choose image
                                        chooseImage();
                                        break;
                                    case 1: // take photo
                                        takePicture();
                                        break;
                                }
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                if (validateRegisterForm()) {
                    String name = mNameEditText.getText().toString().trim();
                    String email = mEmailEditText.getText().toString().trim();
                    String password = mPasswordEditText.getText().toString().trim();

                    registerUser(name, email, password, mChosenImage.getFilePathOriginal());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void registerUser(String name, final String email, String password, String pictureFilePath) {
        mScreenWait.setVisibility(View.VISIBLE);

        WebServices.addUser(name, email, password, pictureFilePath, new WebServices.AddUserCallback() {
            @Override
            public void onFailure(IOException e) {
                mScreenWait.setVisibility(View.GONE);
                showModalOkDialog("Error", "Unable to connect to server.");
            }

            @Override
            public void onResponse(ResponseStatus responseStatus) {
                mScreenWait.setVisibility(View.GONE);

                if (responseStatus.success) {
                    Toast.makeText(RegisterActivity.this, "Register SUCCESS!", Toast.LENGTH_LONG).show();

                    // return to login screen
                    Intent intent = new Intent();
                    intent.putExtra(MainActivity.USERNAME_INTENT_KEY, email);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    showModalOkDialog("Register Failed", responseStatus.message);
                }
            }
        });
    }

    private void takePicture() {
        mImageChooserManager = new ImageChooserManager(
                this,
                ChooserType.REQUEST_CAPTURE_PICTURE,
                true
        );
        mImageChooserManager.setImageChooserListener(this);

        try {
            mImageChooserManager.choose();
        } catch (ChooserException e) {
            e.printStackTrace();
        }
    }

    private void chooseImage() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(Intent.EXTRA_ALLOW_MULTIPLE, false); // ถ้า set เป็น True จะสามารถเลือกภาพ หลายๆภาพพร้อมกันได้

        mImageChooserManager = new ImageChooserManager(
                this,
                ChooserType.REQUEST_PICK_PICTURE,
                true
        );
        mImageChooserManager.setExtras(bundle);
        mImageChooserManager.setImageChooserListener(this);
        mImageChooserManager.clearOldFiles();

        try {
            mImageChooserManager.choose();
        } catch (ChooserException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);

        if (resultCode == RESULT_OK
                && (requestCode == ChooserType.REQUEST_PICK_PICTURE
                || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            if (mImageChooserManager == null) {
                //reinitializeImageChooser();
                return;
            }
            mImageChooserManager.submit(requestCode, returnedIntent);
        }
    }

    @Override
    public void onImageChosen(final ChosenImage image) {
        new Handler(Looper.getMainLooper()).post(
                new Runnable() {
                    @Override
                    public void run() {
                        if (image != null) {
                            mChosenImage = image;

                            String filePathOriginal = image.getFilePathOriginal();
                            Log.i(TAG, "-----");
                            Log.i(TAG, "Image path: " + filePathOriginal);
                            Log.i(TAG, "Image thumbnail path: " + image.getFileThumbnail());
                            Log.i(TAG, "Image small thumbnail path: " + image.getFileThumbnailSmall());

                            Glide.with(RegisterActivity.this)
                                    .load(image.getFileThumbnail())
                                    .into(mPictureImageView);
                        } else {
                            mChosenImage = null;
                            Log.i(TAG, "Image is NULL !?!");
                        }
                    }
                }
        );
    }

    @Override
    public void onError(final String reason) {
        new Handler(Looper.getMainLooper()).post(
                new Runnable() {
                    @Override
                    public void run() {
                        String errMessage = "Error: " + reason;
                        Log.e(TAG, errMessage);
                        Toast.makeText(RegisterActivity.this, errMessage, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @Override
    public void onImagesChosen(ChosenImages chosenImages) {
    }

    private boolean validateRegisterForm() {
        boolean valid = true;

        if ("".equals(mNameEditText.getText().toString().trim())) {
            mNameEditText.setError("Enter name");
            valid = false;
        }
        if ("".equals(mEmailEditText.getText().toString().trim())) {
            mEmailEditText.setError("Enter e-mail");
            valid = false;
        }

        String password = mPasswordEditText.getText().toString().trim();
        String confirmPassword = mConfirmPasswordEditText.getText().toString().trim();
        if ("".equals(password)) {
            mPasswordEditText.setError("Enter password");
            valid = false;
        }
        if ("".equals(confirmPassword)) {
            mConfirmPasswordEditText.setError("Enter password again");
            valid = false;
        } else if (!"".equals(password) && !password.equals(confirmPassword)) {
            mConfirmPasswordEditText.setError("Password and confirm password must be the same");
            valid = false;
        }
        if (valid && mChosenImage == null) {
            Toast.makeText(
                    RegisterActivity.this,
                    "Choose or take a photo to be your profile picture",
                    Toast.LENGTH_LONG
            ).show();

            valid = false;
        }

        return valid;
    }

    private void showModalOkDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .show();
    }
}