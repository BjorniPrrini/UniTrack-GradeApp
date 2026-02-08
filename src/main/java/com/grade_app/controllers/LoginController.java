package com.grade_app.controllers;

import com.grade_app.GradeApp;
import com.grade_app.model.LoginUser;
import com.grade_app.model.User;
import com.grade_app.services.DatabaseConnection;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.geometry.Side;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoginController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passField;

    @FXML
    private Button loginButton;

    private ContextMenu emailSuggestionsMenu;

    private List<String> emails = new ArrayList<>();

    @FXML
    public void initialize(){
        emailSuggestionsMenu = new ContextMenu();

        emails = loadEmailsFromFile();

        setupEmailAutocomplete();
    }

    @FXML
    public void loginHomePage() throws IOException {
        loginButton.setDisable(true);

        String email = emailField.getText().trim();
        String password = passField.getText().trim();

        try {
            DatabaseConnection database = new DatabaseConnection();

            if(email.isEmpty() || password.isEmpty()){
                showAlert("Login Failed", "Empty fields!");

                return;
            }

            if(database.isValidUser(email, password)){
                User user = database.getUser(email);

                if(user == null){
                    showAlert("Login Failed", "User not found!");

                    return;
                }

                saveEmailInFile(email);

                LoginUser.setUser(user);

                switch (user.getRole()) {
                    case "admin" -> {
                        showSuccessAlert("Login Successful", "You have successfully logged in!");

                        GradeApp.setRoot("AdminRegister");
                    }
                    case "teacher" -> {
                        showSuccessAlert("Login Successful", "You have successfully logged in!");

                        GradeApp.setRoot("TeacherGrades");
                    }
                    case "student" -> {
                        showSuccessAlert("Login Successful", "You have successfully logged in!");

                        GradeApp.setRoot("StudentGrades");
                    }
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

    private void saveEmailInFile(String email){
        try{
            File file = new File("src/main/usedEmails.txt");

            boolean found = false;
            String line;

            BufferedReader reader = new BufferedReader(new FileReader(file));

            while((line = reader.readLine()) != null){
                if(line.equals(email)){
                    found = true;

                    break;
                }
            }

            reader.close();

            if(!found){
                BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                writer.write(email);
                writer.newLine();
                emails.add(email);

                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupEmailAutocomplete(){
        emailField.setOnMouseClicked(_ -> showEmailSuggestions());

        emailField.focusedProperty().addListener((_, _, e) -> {
            if(e){
                showEmailSuggestions();
            }
        });

        emailField.textProperty().addListener((_, _, e) -> {
            if(emailField.isFocused()){
                filterAndShowSuggestions(e);
            }
        });
    }

    private void showEmailSuggestions(){
        if(emails.isEmpty()){
            return;
        }

        emailSuggestionsMenu.getItems().clear();

        for(String email : emails){
            MenuItem item = new MenuItem(email);

            item.setOnAction(_ -> {
                emailField.setText(email);
                emailSuggestionsMenu.hide();
                passField.requestFocus();
            });

            emailSuggestionsMenu.getItems().add(item);
        }

        if(!emailSuggestionsMenu.isShowing()){
            emailSuggestionsMenu.show(emailField, Side.BOTTOM, 0, 0);
        }
    }

    private void filterAndShowSuggestions(String text){
        if(emails.isEmpty()){
            emailSuggestionsMenu.hide();

            return;
        }

        emailSuggestionsMenu.getItems().clear();

        for(String email : emails){
            if(email.toLowerCase().contains(text.toLowerCase())){
                MenuItem item = new MenuItem(email);

                item.setOnAction(_ -> {
                    emailField.setText(email);
                    emailSuggestionsMenu.hide();
                    passField.requestFocus();
                });

                emailSuggestionsMenu.getItems().add(item);
            }
        }

        if(!emailSuggestionsMenu.getItems().isEmpty()){
            if(!emailSuggestionsMenu.isShowing()){
                emailSuggestionsMenu.show(emailField, Side.BOTTOM, 0, 0);
            }
        }else{
            emailSuggestionsMenu.hide();
        }
    }

    private List<String> loadEmailsFromFile(){
        List<String> emails = new ArrayList<>();

        File file = new File("src/main/usedEmails.txt");

        if(!file.exists()){
            return emails;
        }

        try {
            String line;

            BufferedReader reader = new BufferedReader(new FileReader(file));

            while((line = reader.readLine()) != null){
                if(!line.trim().isEmpty()){
                    emails.add(line.trim());
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return emails;
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