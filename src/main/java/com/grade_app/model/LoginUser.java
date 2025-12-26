package com.grade_app.model;

public class LoginUser {
    private static User user;

    public static void setUser(User user){
        LoginUser.user = user;
    }

    public static User getUser(){
        return user;
    }

    public static String getEmail(){
        if(user != null){
            return user.getEmail();
        }else{
            return null;
        }
    }
}