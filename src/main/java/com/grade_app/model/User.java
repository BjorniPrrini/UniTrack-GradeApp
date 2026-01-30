package com.grade_app.model;

public class User {
    private final int userID;
    private final String name;
    private final String surname;
    private final String email;
    private final String role;

    public User(int userID, String name, String surname, String email, String role){
        this.userID = userID;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.role = role;
    }
    
    public int getUserID(){
        return userID;
    }

    public String getName(){
        return name;
    }

    public String getSurname(){
        return surname;
    }

    public String getEmail(){
        return email;
    }

    public String getRole(){
        return role;
    }
}