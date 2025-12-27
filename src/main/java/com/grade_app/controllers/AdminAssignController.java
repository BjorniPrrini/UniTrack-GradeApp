package com.grade_app.controllers;

import com.grade_app.GradeApp;
import com.grade_app.model.User;
import com.grade_app.services.DatabaseConnection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class AdminAssignController {
    @FXML
    private Button insertButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button assignButton;

    @FXML
    private Button logOutButton;

    @FXML
    private Button subjectButton;

    @FXML
    private TextField teacherNameField;

    @FXML
    private TextField teacherSurnameField;

    @FXML
    private ToggleGroup optionToggleGroup;

    @FXML
    private Button loadSubjectButton;

    @FXML
    private ComboBox<String> subjectComboBox;

    @FXML
    private Button submitButton;

    @FXML
    public void adminRegister() throws IOException {
        GradeApp.setRoot("AdminRegister");
    }

    @FXML
    public void adminDelete(ActionEvent actionEvent) throws IOException {
        GradeApp.setRoot("AdminDelete");
    }

    @FXML
    public void adminAssign(ActionEvent actionEvent) throws IOException {
        GradeApp.setRoot("AdminAssign");
    }

    @FXML
    public void adminSubject(ActionEvent actionEvent) throws IOException {
        GradeApp.setRoot("AdminSubjects");
    }

    @FXML
    public void logOut(ActionEvent actionEvent) throws IOException {
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
    public void initialize() {
        insertButton.setFocusTraversable(false);
        deleteButton.setFocusTraversable(false);
        assignButton.setFocusTraversable(true);
        logOutButton.setFocusTraversable(false);
        subjectButton.setFocusTraversable(false);
    }

    private String chosenSubject = "";

    @FXML
    public void loadSubjects(){
        loadSubjectButton.setDisable(true);

        try{
            DatabaseConnection database = new DatabaseConnection();

            String name = teacherNameField.getText();
            String surname = teacherSurnameField.getText();
            String choice = ((RadioButton) optionToggleGroup.getSelectedToggle()).getText();

            if(name.isEmpty() || surname.isEmpty() || choice == null){
                showAlert("Empty fields", "Please fill all the fields or radio button!");

                return;
            }

            User teacher = database.searchUser(name, surname);

            if(teacher == null){
                showAlert("User not found", "User could not be found!");

                return;
            }

            int teacherID = teacher.getUserID();

            if(choice.equalsIgnoreCase("Add")){
                subjectComboBox.getItems().clear();
                subjectComboBox.getItems().addAll(database.getSubjectNotInTeacher(teacherID));
                subjectComboBox.setOnAction(e -> {
                    chosenSubject = subjectComboBox.getValue();
                });
            }else if(choice.equalsIgnoreCase("Delete")){
                subjectComboBox.getItems().clear();
                subjectComboBox.getItems().addAll(database.getSubjectInTeacher(teacherID));
                subjectComboBox.setOnAction(e -> {
                    chosenSubject = subjectComboBox.getValue();
                });
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            loadSubjectButton.setDisable(false);
        }
    }

    public void submit() {
        submitButton.setDisable(true);

        try{
            DatabaseConnection database = new DatabaseConnection();

            Toggle selectedToggle = optionToggleGroup.getSelectedToggle();
            if(selectedToggle == null){
                showAlert("Selection Error", "Please select Add or Delete");

                return;
            }

            String choice = ((RadioButton) selectedToggle).getText();

            String name = teacherNameField.getText();
            String surname = teacherSurnameField.getText();

            User teacher = database.searchUser(name, surname);
            int teacherID = teacher.getUserID();

            boolean successful = database.submitAssignedSubject(teacherID, chosenSubject, choice);

            if(successful){
                showSuccessAlert("Submitted", "Submitted successfully!");
            }else{
                showAlert("Error", "An error occurred!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            submitButton.setDisable(false);
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