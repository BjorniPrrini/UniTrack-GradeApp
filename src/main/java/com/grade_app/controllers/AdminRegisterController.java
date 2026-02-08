package com.grade_app.controllers;

import com.grade_app.GradeApp;
import com.grade_app.model.Course;
import com.grade_app.model.Subject;
import com.grade_app.services.DatabaseConnection;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AdminRegisterController {
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
    public void adminRegister() throws IOException {
        GradeApp.setRoot("AdminRegister");
    }

    @FXML
    public void adminDelete() throws IOException {
        GradeApp.setRoot("AdminDelete");
    }

    @FXML
    public void adminAssign() throws IOException {
        GradeApp.setRoot("AdminAssign");
    }

    @FXML
    public void adminSubject() throws IOException {
        GradeApp.setRoot("AdminSubjects");
    }

    @FXML
    public void changePassword() throws IOException {
        GradeApp.setRoot("PasswordChanger");
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
    public void initialize() throws SQLException {
        DatabaseConnection database = new DatabaseConnection();

        roleComboBox.getItems().addAll("admin", "student", "teacher");
        roleComboBox.setValue("student");

        List<Course> courses = database.getCourses();

        courseComboBox.getItems().addAll(courses);

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
                    yearComboBox.setVisible(false);
                    yearComboBox.setManaged(false);
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
                    subjectsComboBox.setVisible(false);
                    subjectsComboBox.setManaged(false);
                }
            }
        });

        courseComboBox.getSelectionModel().selectedItemProperty().addListener((_, _, course) -> {
            if(course != null && "teacher".equals(roleComboBox.getValue())){
                List<Subject> subjects = database.getSubjects(course.getId());

                subjectsComboBox.getItems().setAll(subjects);
            }else if("student".equalsIgnoreCase(roleComboBox.getValue())){
                updateYearComboBox(course);
            }
        });
    }

    private void updateYearComboBox(Course course){
        yearComboBox.getItems().clear();

        if(course != null){
            int duration = course.getDuration();

            for(int i = 1; i <= duration; i++){
                yearComboBox.getItems().add(i);
            }

            if(yearComboBox.getItems().isEmpty()){
                yearComboBox.setValue(1);
            }
        }
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

        if(name == null || name.isEmpty() || surname == null || surname.isEmpty()){
            showAlert("Empty fields", "The name or surname field is empty!");

            return;
        }

        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        surname = surname.substring(0, 1).toUpperCase() + surname.substring(1);
        String email = generatedEmailField.getText();
        String password = generatedPasswordField.getText();
        String role = roleComboBox.getValue();

        Integer year;

        if(role.equals("student")){
            year = yearComboBox.getValue();
        }else{
            year = 0;
        }

        Course course = courseComboBox.getValue();
        Subject subject = subjectsComboBox.getValue();

        try {
            DatabaseConnection database = new DatabaseConnection();

            boolean success = database.adminRegister(name, surname, email, password, role, year, course, subject);

            if(success){
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
            }else{
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