package com.example.onlinequiz.model;

/**
 * Created by Administrator on 21/5/2559.
 */
public class User {

    public final int userId;
    public final String name;
    public final String username;
    public final String picture;

    public User(int userId, String name, String username, String picture) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.picture = picture;
    }

    public static User loggedInUser = null;

}
