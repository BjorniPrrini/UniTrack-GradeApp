package com.grade_app.controllers;

import com.grade_app.GradeApp;
import com.grade_app.model.LoginUser;
import com.grade_app.model.User;
import com.grade_app.services.DatabaseConnection;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {
    @FXML
    private TextField emailField;

    @FXML
   private PasswordField passField;

    @FXML
    private Button loginButton;

    public void loginHomePage() throws IOException {
        loginButton.setDisable(true);

        String email = emailField.getText().trim();
        String password = passField.getText().trim();

        try {
            DatabaseConnection database = new DatabaseConnection();

            if (email.isEmpty() || password.isEmpty()) {
                showAlert("Login Failed", "Empty fields!");

                return;
            }

            if(database.isValidUser(email, password)) {
                User user = database.getUser(email);

                if(user == null) {
                    showAlert("Login Failed", "User not found!");

                    return;
                }

                LoginUser.setUser(user);

                if(user.getRole().equals("admin")) {
                    showSuccessAlert("Login Successful", "You have successfully logged in!");

                    GradeApp.setRoot("AdminRegister");
                }else if(user.getRole().equals("teacher")){
                    showSuccessAlert("Login Successful", "You have successfully logged in!");

                    GradeApp.setRoot("TeacherGrades");
                }else if(user.getRole().equals("student")){
                    showSuccessAlert("Login Successful", "You have successfully logged in!");

                    GradeApp.setRoot("StudentGrades");
                }
            }else{
                showAlert("Login Failed", "Invalid username or password!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            loginButton.setDisable(false);
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