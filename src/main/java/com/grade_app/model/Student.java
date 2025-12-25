package com.grade_app.model;

public class Student extends User{
    private int year;

    public Student(int userID, String name, String surname, String email, String role, int year) {
        super(userID, name, surname, email, role);
        this.year = year;
    }

    public int getYear(){
        return year;
    }
}