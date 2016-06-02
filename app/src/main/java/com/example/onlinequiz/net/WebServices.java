package com.example.onlinequiz.net;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.onlinequiz.model.Choice;
import com.example.onlinequiz.model.Question;
import com.example.onlinequiz.model.Quiz;
import com.example.onlinequiz.model.ResponseStatus;
import com.example.onlinequiz.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 21/5/2559.
 */
public class WebServices {

    private static final String TAG = WebServices.class.getSimpleName();

    private static final String BASE_URL = "http://10.0.3.2/online_quiz/";
    private static final String LOGIN_URL = BASE_URL + "login.php?username=%s&password=%s";
    private static final String ADD_USER_URL = BASE_URL + "add_user.php";
    private static final String GET_QUIZZES_URL = BASE_URL + "quiz_index.php";
    public static final String USER_IMAGE_URL = BASE_URL + "user_images/";
    private static final String IMAGES_BASE_URL = BASE_URL + "images/";
    private static final String GET_QUESTIONS_URL = BASE_URL + "get_questions.php?quiz_id=%d";
    private static final String SET_USER_GUESSES_URL = BASE_URL + "set_user_guesses.php";



    private static final OkHttpClient mClient = new OkHttpClient();


    private static User mUser;
    private static ResponseStatus mResponseStatus;
    private static ArrayList<Quiz> mQuizArrayList;
    private static ArrayList<Question> mQuestionArrayList;

    public interface LoginCallback {
        void onFailure(IOException e);

        void onResponse(ResponseStatus responseStatus, User user);
    }

    public interface AddUserCallback {
        void onFailure(IOException e);

        void onResponse(ResponseStatus responseStatus);
    }

    public interface GetQuestionsCallback {
        void onFailure(IOException e);
        void onResponse(ResponseStatus responseStatus, ArrayList<Question> questionArrayList);
    }

    public interface SetUserGuessesCallback {
        void onFailure(IOException e);
        void onResponse(ResponseStatus responseStatus);
    }

    public interface GetQuizzesCallback {
        void onFailure(IOException e);

        void onResponse(ResponseStatus responseStatus, ArrayList<Quiz> quizArrayList);
    }


    public static void login(String username,
                             String password,
                             final LoginCallback callback) {

        Request request = new Request.Builder()
                .url(String.format(LOGIN_URL, username, password))
                .build();

        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                new Handler(Looper.getMainLooper()).post(   //ทำให้ Handler ผูกกับ Looper ของ MainThread ทำให้ยุ่งกับ UI ได้
                        new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure(e);
                            }
                        }
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonResult = response.body().string();

                try {
                    JSONObject jsonObject = new JSONObject(jsonResult);
                    int success = jsonObject.getInt("success");

                    if (success == 1) {
                        mResponseStatus = new ResponseStatus(true, null);
                        //เชื่อมต่อ MySql สำเร็จ
                        int loginSuccess = jsonObject.getInt("login_success");

                        if (loginSuccess == 1) {
                            //login สำเร็จ
                            int userId = jsonObject.getInt("user_id");
                            String name = jsonObject.getString("name");
                            String username = jsonObject.getString("username");
                            String picture = jsonObject.getString("picture");

                            mUser = new User(userId, name, username, picture);
                        } else if (loginSuccess == 0) {
                            //login ไม่สำเร็จ
                            mUser = null;
                        }
                    } else if (success == 0) {
                        //เชื่อมต่อ MySql ไม่สำเร็จ
                        mResponseStatus = new ResponseStatus(false, jsonObject.getString("message"));
                        mUser = null;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error parsing JSON");
                }


                new Handler(Looper.getMainLooper()).post(   //ทำให้ Handler ผูกกับ Looper ของ MainThread ทำให้ยุ่งกับ UI ได้
                        new Runnable() {
                            @Override
                            public void run() {
                                callback.onResponse(mResponseStatus, mUser);
                            }
                        }
                );


            }
        });
    }

    private static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpg");

    public static void addUser(String name, String email, String password, String pictureFilePath,
                               final AddUserCallback callback) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", name)
                .addFormDataPart("username", email)
                .addFormDataPart("password", password)
                .addFormDataPart("picture", "picture.jpg",
                        RequestBody.create(MEDIA_TYPE_JPEG, new File(pictureFilePath)))
                .build();

        Request request = new Request.Builder()
                .url(ADD_USER_URL)
                .post(requestBody)
                .build();

        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure(e);
                            }
                        }
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //delay(1);

                final String jsonResult = response.body().string();
                Log.d(TAG, jsonResult);

                try {
                    JSONObject jsonObject = new JSONObject(jsonResult);
                    int success = jsonObject.getInt("success");

                    if (success == 1) {
                        mResponseStatus = new ResponseStatus(true, jsonObject.getString("message"));
                    } else if (success == 0) {
                        mResponseStatus = new ResponseStatus(false, jsonObject.getString("message"));
                    }
                } catch (JSONException e) {
                    mResponseStatus = new ResponseStatus(false, "Error parsing JSON.");
                    Log.e(TAG, "Error parsing JSON.");
                    e.printStackTrace();
                }

                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                callback.onResponse(mResponseStatus);
                            }
                        }
                );
            }
        });
    }

    public static void getQuizzes(final GetQuizzesCallback callback) {
        Request request = new Request.Builder()
                .url(GET_QUIZZES_URL)
                .build();

        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure(e);
                            }
                        }
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String jsonResult = response.body().string();
                Log.d(TAG, jsonResult);


                try {
                    JSONObject jsonObject = new JSONObject(jsonResult);
                    int success = jsonObject.getInt("success");

                    if (success == 1) {
                        mResponseStatus = new ResponseStatus(true, null);
                        mQuizArrayList = new ArrayList<>();

                        parseJsonQuizData(jsonObject.getJSONArray("quiz_data"));
                    } else if (success == 0) {
                        mResponseStatus = new ResponseStatus(false, jsonObject.getString("message"));
                        mQuizArrayList = null;
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON.");
                    e.printStackTrace();
                }

                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                callback.onResponse(mResponseStatus, mQuizArrayList);
                            }
                        }
                );
            }
        });
    }

    private static void parseJsonQuizData(JSONArray jsonArrayQuizData) throws JSONException {
        for (int i = 0; i < jsonArrayQuizData.length(); i++) {
            JSONObject jsonQuiz = jsonArrayQuizData.getJSONObject(i);

            Quiz quiz = new Quiz(
                    jsonQuiz.getInt("quiz_id"),
                    jsonQuiz.getString("title"),
                    jsonQuiz.getString("detail"),
                    jsonQuiz.getInt("number_of_questions")
            );
            mQuizArrayList.add(quiz);
        }
    }

    public static void getQuestions(int quizId, final GetQuestionsCallback callback) {
        Request request = new Request.Builder()
                .url(String.format(GET_QUESTIONS_URL, quizId))
                .build();

        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure(e);
                            }
                        }
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {


                final String jsonResult = response.body().string();
                Log.d(TAG, jsonResult);

                try {
                    JSONObject jsonObject = new JSONObject(jsonResult);
                    int success = jsonObject.getInt("success");

                    if (success == 1) {
                        mResponseStatus = new ResponseStatus(true, null);
                        mQuestionArrayList = new ArrayList<>();

                        parseJsonQuestionData(
                                jsonObject.getJSONArray("question_data"),
                                jsonObject.getInt("quiz_id")
                        );
                    } else if (success == 0) {
                        mResponseStatus = new ResponseStatus(false, jsonObject.getString("message"));
                        mQuestionArrayList = null;
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON.");
                    e.printStackTrace();
                }

                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                callback.onResponse(mResponseStatus, mQuestionArrayList);
                            }
                        }
                );
            }
        });
    }

    private static void parseJsonQuestionData(JSONArray jsonArrayQuestionData, int quizId) throws JSONException {
        for (int i = 0; i < jsonArrayQuestionData.length(); i++) {
            JSONObject jsonQuestion = jsonArrayQuestionData.getJSONObject(i);

            String pictureFilename = null;
            if (!jsonQuestion.isNull("picture")) {
                pictureFilename = IMAGES_BASE_URL
                        + String.valueOf(quizId).trim()
                        + "/"
                        + jsonQuestion.getString("picture");
            }

            Question question = new Question(
                    jsonQuestion.getInt("question_id"),
                    jsonQuestion.getString("title"),
                    jsonQuestion.getString("detail").replace("\\n", "\n"),
                    pictureFilename
            );

            JSONArray jsonArrayChoiceData = jsonQuestion.getJSONArray("choice_data");
            for (int j = 0; j < jsonArrayChoiceData.length(); j++) {
                JSONObject jsonChoice = jsonArrayChoiceData.getJSONObject(j);

                Choice choice = new Choice(
                        jsonChoice.getInt("choice_id"),
                        jsonChoice.getString("text"),
                        jsonChoice.getBoolean("is_answer")
                );
                question.choiceArrayList.add(choice);
            }

            mQuestionArrayList.add(question);
        }
    }

    public static void setUserGuesses(int userId, int quizId,
                                      ArrayList<Question> questionArrayList,
                                      final SetUserGuessesCallback callback) {

        FormBody.Builder builder = new FormBody.Builder()
                .add("user_id", String.valueOf(userId))
                .add("quiz_id", String.valueOf(quizId));

        for (Question question : questionArrayList) {
            builder.add("question_id[]", String.valueOf(question.questionId));
            builder.add("choice_id[]", String.valueOf(question.getSelectedChoiceId()));
        }

        RequestBody formBody = builder.build();

        Request request = new Request.Builder()
                .url(SET_USER_GUESSES_URL)
                .post(formBody)
                .build();

        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure(e);
                            }
                        }
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String jsonResult = response.body().string();
                Log.d(TAG, jsonResult);

                try {
                    JSONObject jsonObject = new JSONObject(jsonResult);
                    int success = jsonObject.getInt("success");

                    if (success == 1) {
                        mResponseStatus = new ResponseStatus(true, jsonObject.getString("message"));
                    } else if (success == 0) {
                        mResponseStatus = new ResponseStatus(false, jsonObject.getString("message"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON.");
                    e.printStackTrace();
                }

                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                callback.onResponse(mResponseStatus);
                            }
                        }
                );
            }
        });
    }
}
