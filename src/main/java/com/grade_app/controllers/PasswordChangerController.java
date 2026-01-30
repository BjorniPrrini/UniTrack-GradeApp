package com.grade_app.controllers;

import com.grade_app.GradeApp;
import com.grade_app.model.LoginUser;

import com.grade_app.services.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

public class PasswordChangerController {
    @FXML
    public TextField oldPassword;

    @FXML
    public TextField newPassword;

    @FXML
    public Button changePasswordButton;

    @FXML
    public void changePassword() {
        String oldPasswordText = oldPassword.getText();
        String newPasswordText = newPassword.getText();

        try{
            DatabaseConnection database = new DatabaseConnection();

            if(database.changePassword(LoginUser.getUser().getEmail(), oldPasswordText, newPasswordText)){
                showSuccessAlert("Password changed", "Password was successfully changed!");
            }else{
                showAlert("Incorrect", "Password could not be changed!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack() throws IOException {
        if(LoginUser.getUser().getRole().equalsIgnoreCase("student")){
            GradeApp.setRoot("StudentProfile");
        }else if(LoginUser.getUser().getRole().equalsIgnoreCase("admin")){
            GradeApp.setRoot("AdminRegister");
        }else if(LoginUser.getUser().getRole().equalsIgnoreCase("teacher")){
            GradeApp.setRoot("TeacherProfile");
        }
    }

    private void showAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}