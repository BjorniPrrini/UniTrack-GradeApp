package com.grade_app.model;

import java.util.ArrayList;
import java.util.List;

public class StudentRow {
    private final int studentID;
    private final String studentName;
    private final List<GradeData> grades = new ArrayList<>();
    private double finalGrade;

    public StudentRow(int studentID, String studentName){
        this.studentID = studentID;
        this.studentName = studentName;
    }

    public int getStudentID() {
        return studentID;
    }

    public String getStudentName() {
        return studentName;
    }

    public List<GradeData> getGrades() {
        return grades;
    }

    public double getFinalGrade() {
        return finalGrade;
    }

    public String getLetterGrade() {
        return getGradeLetter(finalGrade);
    }

    private String getGradeLetter(double finalGrade){
        if(finalGrade <= 100 && finalGrade >= 90){
            return "AA";
        }else if(finalGrade <= 89 && finalGrade >= 85){
            return "BA";
        }else if(finalGrade <= 84 && finalGrade >= 80){
            return "BB";
        }else if(finalGrade <= 79 && finalGrade >= 75){
            return "CB";
        }else if(finalGrade <= 74 && finalGrade >= 70){
            return "CC";
        }else if(finalGrade <= 69 && finalGrade >= 65){
            return "DC";
        }else if(finalGrade <= 64 && finalGrade >= 60){
            return "DD";
        }else if(finalGrade <= 59 && finalGrade >= 50){
            return "FD";
        }else{
            return "FF";
        }
    }

    public void addGrade(String assessmentName, int gradeValue, int gradeID, int weight) {
        grades.add(new GradeData(assessmentName, gradeID, gradeValue, weight));
    }

    public int getGradeForAssessment(String assessmentName) {
        for(GradeData grade : grades){
            if(grade.assessmentName().equals(assessmentName)){
                return grade.gradeValue();
            }
        }

        return 0;
    }

    public Integer getGradeIDForAssessment(String assessmentName) {
        for(GradeData grade : grades){
            if(grade.assessmentName().equals(assessmentName)){
                return grade.gradeID();
            }
        }

        return null;
    }

    public void updateGrade(String assessmentName, int newValue) {
        for(int i = 0; i < grades.size(); i++){
            GradeData grade = grades.get(i);

            if(grade.assessmentName().equals(assessmentName)){
                grades.set(i, new GradeData(grade.assessmentName(), grade.gradeID(), newValue, grade.weight()));

                break;
            }
        }

        calculateFinalGrade();
    }

    public void calculateFinalGrade() {
        double sum = 0;
        int weightSum = 0;

        for(GradeData grade : grades){
            sum += grade.gradeValue() * grade.weight();
            weightSum += grade.weight();
        }

        finalGrade = weightSum > 0 ? sum / weightSum : 0.0;
    }

    public record GradeData(String assessmentName, int gradeID, int gradeValue, int weight) {}
}