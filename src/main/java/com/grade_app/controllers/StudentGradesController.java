package com.grade_app.controllers;

import com.grade_app.GradeApp;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.util.Optional;

public class StudentGradesController {
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("ARE YOU SURE YOU WANT TO LOG OUT?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            GradeApp.setRoot("LogInPage");
        }
    }

    @FXML
    public void initialize(){
        studentProfileButton.setFocusTraversable(false);
        studentGradesButton.setFocusTraversable(true);
    }
}