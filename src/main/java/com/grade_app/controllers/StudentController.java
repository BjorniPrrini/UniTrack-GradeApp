package com.grade_app.controllers;

import com.grade_app.GradeApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Optional;

public class StudentController {
    @FXML
    private BorderPane studentBorderPane;

    @FXML
    private VBox studentCenterVbox;

    public void openStudentProfile() throws IOException{
        Parent profileView = FXMLLoader.load(getClass().getResource("/com/ui/StudentProfile.fxml"));
        studentBorderPane.setCenter(profileView);
    }
    public void openStudentGrades() throws IOException{
        studentBorderPane.setCenter(studentCenterVbox);
    }
    public void logOut() throws  IOException{
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to log out?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            GradeApp.setRoot("LogInPage");
        }
    }
}
