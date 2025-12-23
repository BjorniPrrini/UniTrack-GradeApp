package com.grade_app.controllers;

import com.grade_app.GradeApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class TeacherProfileController {
    @FXML
    private Button teacherGradesButton;

    @FXML
    private Button teacherProfileButton;

    @FXML
    public void teacherGrades() throws IOException {
        GradeApp.setRoot("TeacherGrades");
    }

    @FXML
    public void teacherProfile() throws IOException {
        GradeApp.setRoot("TeacherProfile");
    }

    @FXML
    public void logOut() throws IOException {
        GradeApp.setRoot("LogInPage");
    }

    @FXML
    public void initialize(){
        teacherProfileButton.setFocusTraversable(true);
        teacherGradesButton.setFocusTraversable(false);
    }
}