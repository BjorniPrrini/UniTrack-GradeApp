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

public class AdminController {
    @FXML
   private BorderPane adminBorderPane;

    @FXML
    private VBox adminVboxCenter;

    public void adminDelete() throws IOException{
        Parent deleteView = FXMLLoader.load(getClass().getResource("/com/ui/AdminDelete.fxml"));
        adminBorderPane.setCenter(deleteView);
    }
    public void adminInsert() throws  IOException{
        adminBorderPane.setCenter(adminVboxCenter);
    }
    public void adminAssign() throws  IOException{
        Parent assignView = FXMLLoader.load(getClass().getResource("/com/ui/AdminAssign.fxml"));
        adminBorderPane.setCenter(assignView);
    }
    public void adminSubject() throws IOException{
        Parent subjectView = FXMLLoader.load(getClass().getResource("/com/ui/AdminSubjects.fxml"));
        adminBorderPane.setCenter(subjectView);
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

