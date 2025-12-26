package com.grade_app.controllers;

import com.grade_app.GradeApp;
import com.grade_app.model.LoginUser;
import com.grade_app.model.Student;
import com.grade_app.services.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class StudentProfileController {
    @FXML
    private Button studentProfileButton;

    @FXML
    private Button studentGradesButton;

    @FXML
    private Label studentNameLabel;

    @FXML
    private Label studentSurnameLabel;

    @FXML
    private Label studentYearLabel;

    @FXML
    private Label studentDegreeLabel;

    @FXML
    public void openStudentGrades() throws IOException {
        GradeApp.setRoot("StudentGrades");
    }

    @FXML
    public void openStudentProfile() throws IOException {
        GradeApp.setRoot("StudentProfile");
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
        studentProfileButton.setFocusTraversable(true);
        studentGradesButton.setFocusTraversable(false);

        Student student = (Student) LoginUser.getUser();

        studentNameLabel.setText(student.getName() + "  " + student.getSurname());
        studentSurnameLabel.setText(student.getEmail());
        studentYearLabel.setText("Year: " + student.getYear());

        try{
            DatabaseConnection database = new DatabaseConnection();
            studentDegreeLabel.setText(database.getCoursePerStudent(student.getUserID()));
        }catch (SQLException e){
            e.printStackTrace();
        }

    }
}