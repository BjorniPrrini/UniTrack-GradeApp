package com.grade_app.controllers;

import com.grade_app.GradeApp;
import com.grade_app.model.Course;
import com.grade_app.model.Subject;
import com.grade_app.services.DatabaseConnection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AdminRegisterController {
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
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private ComboBox<Course> courseComboBox;

    @FXML
    private ComboBox<Subject> subjectsComboBox;

    @FXML
    private ComboBox<Integer> yearComboBox;

    @FXML
    private TextField generatedEmailField;

    @FXML
    private TextField generatedPasswordField;

    @FXML
    private Button registerButton;

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
        GradeApp.setRoot("LogInPage");
    }

    @FXML
    public void initialize() throws SQLException {
        DatabaseConnection database = new DatabaseConnection();

        insertButton.setFocusTraversable(true);
        deleteButton.setFocusTraversable(false);
        assignButton.setFocusTraversable(false);
        logOutButton.setFocusTraversable(false);
        subjectButton.setFocusTraversable(false);

        roleComboBox.getItems().addAll("admin", "student", "teacher");
        roleComboBox.setValue("student");

        List<Course> courses = database.getCourses();

        courseComboBox.getItems().addAll(courses);

        yearComboBox.getItems().addAll(1, 2, 3, 4, 5);

        generatedEmailField.setEditable(false);
        generatedPasswordField.setEditable(false);

        subjectsComboBox.setVisible(false);
        subjectsComboBox.setManaged(false);

        roleComboBox.valueProperty().addListener((_, _, role) -> {
            switch (role) {
                case "teacher" -> {
                    courseComboBox.setVisible(true);
                    courseComboBox.setManaged(true);
                    subjectsComboBox.setVisible(true);
                    subjectsComboBox.setManaged(true);
                }
                case "admin" -> {
                    courseComboBox.setVisible(false);
                    courseComboBox.setManaged(false);
                    yearComboBox.setVisible(false);
                    yearComboBox.setManaged(false);
                    subjectsComboBox.setVisible(false);
                    subjectsComboBox.setManaged(false);
                }
                case "student" -> {
                    courseComboBox.setVisible(true);
                    courseComboBox.setManaged(true);
                    yearComboBox.setVisible(true);
                    yearComboBox.setManaged(true);
                }
            }
        });

        courseComboBox.getSelectionModel().selectedItemProperty().addListener((_, _, newCourse) -> {
            if (newCourse != null && "teacher".equals(roleComboBox.getValue())) {
                List<Subject> subjects = database.getSubjects(newCourse.getId());

                subjectsComboBox.getItems().setAll(subjects);
            }
        });
    }

    @FXML
    public void generateEmailAndPassword() {
        String name = nameField.getText();
        String surname = surnameField.getText();

        int year = LocalDate.now().getYear() % 100;
        String email = name.toLowerCase() + surname.toLowerCase() + year + "@gmail.com";

        String password = name.toLowerCase() + surname.toLowerCase() + year;

        generatedEmailField.setText(email);
        generatedPasswordField.setText(password);
    }

    @FXML
    public void register() {
        String name = nameField.getText();
        String surname = surnameField.getText();
        String email = generatedEmailField.getText();
        String password = generatedPasswordField.getText();
        String role = roleComboBox.getValue();
        Integer year = yearComboBox.getValue();
        Course course = courseComboBox.getValue();
        Subject subject = subjectsComboBox.getValue();

        try {
            DatabaseConnection database = new DatabaseConnection();

            boolean success = database.adminRegister(name, surname, email, password, role, year, course, subject);

            if (success) {
                showSuccessAlert("Registered", "User registered successfully!");
                nameField.clear();
                surnameField.clear();
                generatedEmailField.clear();
                generatedPasswordField.clear();

                roleComboBox.setValue("student");
                subjectsComboBox.getSelectionModel().clearSelection();

                courseComboBox.setVisible(true);
                courseComboBox.setManaged(true);
                yearComboBox.setVisible(true);
                yearComboBox.setManaged(true);
                subjectsComboBox.setVisible(false);
                subjectsComboBox.setManaged(false);
            } else {
                showAlert("Error", "User could not be registered!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
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