package com.grade_app.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentRow {
    private final int studentID;
    private final String studentName;
    private final Map<String, Integer> grades;
    private final Map<String, Integer> gradeIDs;
    private double finalGrade;

    public StudentRow(int studentID, String studentName){
        this.studentID = studentID;
        this.studentName = studentName;
        this.grades = new HashMap<>();
        this.gradeIDs = new HashMap<>();
        this.finalGrade = 0.0;
    }

    public int getStudentID(){
        return studentID;
    }

    public String getStudentName(){
        return studentName;
    }

    public Map<String, Integer> getGrades(){
        return grades;
    }

    public Map<String, Integer> getGradeIDs(){
        return gradeIDs;
    }

    public double getFinalGrade(){
        return finalGrade;
    }

    public void addGrade(String assessmentName, int gradeValue, int gradeID){
        grades.put(assessmentName, gradeValue);
        gradeIDs.put(assessmentName, gradeID);
    }

    public void calculateAverage(List<String> assessmentNames, Map<String, Integer> assessmentWeights){
        double weightedSum = 0.0;
        int totalWeight = 0;

        for(String assessment : assessmentNames){
            Integer grade = grades.get(assessment);

            if(grade != null){
                int weight = assessmentWeights.getOrDefault(assessment, 0);

                weightedSum += grade * weight;
                totalWeight += weight;
            }
        }

        if(totalWeight > 0){
            finalGrade = weightedSum / totalWeight;
        }else{
            finalGrade = 0.0;
        }
    }

    public int getGradeForAssessment(String assessmentName){
        return grades.getOrDefault(assessmentName, 0);
    }

    public Integer getGradeIDForAssessment(String assessmentName){
        return gradeIDs.get(assessmentName);
    }
}