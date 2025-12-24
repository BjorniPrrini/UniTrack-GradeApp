package com.grade_app.model;

public class Subject {
    private int id;
    private String name;
    private int credits;

    public Subject(int id, String name, int credits){
        this.id = id;
        this.name = name;
        this.credits = credits;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public int getCredits(){
        return credits;
    }

    @Override
    public String toString(){
        return name;
    }
}