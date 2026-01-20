package com.grade_app.model;

public class Assessment {
    private final int subjectAssessmentID;
    private final String name;
    private final int weight;
    private final int gradeValue;

    public Assessment(int subjectAssessmentID, String name, int weight, int gradeValue){
        this.subjectAssessmentID = subjectAssessmentID;
        this.name = name;
        this.weight = weight;
        this.gradeValue = gradeValue;
    }

    public int getSubjectAssessmentID(){
        return subjectAssessmentID;
    }

    public String getName(){
        return name;
    }

    public int getWeight(){
        return weight;
    }

    public int getGradeValue(){
        return gradeValue;
    }
}