package com.grade_app.model;

public class StudentGrade {
    private int studentID;
    private String studentName;
    private int gradeID;
    private int subjectAssessmentID;
    private String assessmentName;
    private int gradeValue;
    private boolean finalized;

    public StudentGrade(int studentID, String studentName, int gradeID, int subjectAssessmentID, String assessmentName, int gradeValue, boolean finalized){
        this.studentID = studentID;
        this.studentName = studentName;
        this.gradeID = gradeID;
        this.subjectAssessmentID = subjectAssessmentID;
        this.assessmentName = assessmentName;
        this.gradeValue = gradeValue;
        this.finalized = finalized;
    }

    public int getStudentID(){
        return studentID;
    }

    public void setStudentID(int studentID){
        this.studentID = studentID;
    }

    public String getStudentName(){
        return studentName;
    }

    public void setStudentName(String studentName){
        this.studentName = studentName;
    }

    public int getGradeID(){
        return gradeID;
    }

    public void setGradeID(int gradeID){
        this.gradeID = gradeID;
    }

    public int getSubjectAssessmentID(){
        return subjectAssessmentID;
    }

    public void setSubjectAssessmentID(int subjectAssessmentID){
        this.subjectAssessmentID = subjectAssessmentID;
    }

    public String getAssessmentName(){
        return assessmentName;
    }

    public void setAssessmentName(String assessmentName){
        this.assessmentName = assessmentName;
    }

    public int getGradeValue(){
        return gradeValue;
    }

    public void setGradeValue(int gradeValue){
        this.gradeValue = gradeValue;
    }

    public boolean isFinalized(){
        return finalized;
    }

    public void setFinalized(boolean finalized){
        this.finalized = finalized;
    }
}