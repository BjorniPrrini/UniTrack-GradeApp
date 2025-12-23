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

public class TeacherController {
    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private VBox centerVbox;

    @FXML
    public void openProfile() throws IOException{
        Parent profileView = FXMLLoader.load(getClass().getResource("/com/ui/TeacherProfile.fxml"));

        rootBorderPane.setCenter(profileView);
    }
    @FXML
    public void openHome() throws  IOException{
        rootBorderPane.setCenter(centerVbox);
    }

    @FXML
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