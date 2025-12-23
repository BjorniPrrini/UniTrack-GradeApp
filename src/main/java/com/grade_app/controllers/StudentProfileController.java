package com.grade_app.controllers;

import com.grade_app.GradeApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class StudentProfileController {
    @FXML
    private Button studentProfileButton;

    @FXML
    private Button studentGradesButton;

    @FXML
    public void openStudentGrades() throws IOException {
        GradeApp.setRoot("StudentGrades");
    }

    @FXML
    public void openStudentProfile() throws IOException {
        GradeApp.setRoot("StudentProfile");
    }

    @FXML
    public void logOut() throws IOException {
        GradeApp.setRoot("LogInPage");
    }

    @FXML
    public void initialize(){
        studentProfileButton.setFocusTraversable(true);
        studentGradesButton.setFocusTraversable(false);
    }
}