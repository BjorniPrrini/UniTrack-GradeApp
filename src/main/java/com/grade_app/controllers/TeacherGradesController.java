package com.grade_app.controllers;

import com.grade_app.GradeApp;

import com.grade_app.model.LoginUser;
import com.grade_app.services.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class TeacherGradesController {
    @FXML
    private Button teacherGradesButton;

    @FXML
    private Button teacherProfileButton;

    @FXML
    private ComboBox<String> subjectComboBox;

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
        teacherProfileButton.setFocusTraversable(false);
        teacherGradesButton.setFocusTraversable(true);

        try{
            DatabaseConnection database = new DatabaseConnection();

            subjectComboBox.getItems().addAll(database.getSubjectInTeacher(LoginUser.getUser().getUserID()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}