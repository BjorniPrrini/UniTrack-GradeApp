package com.grade_app.controllers;

import com.grade_app.GradeApp;
import com.grade_app.model.LoginUser;
import com.grade_app.model.Student;
import com.grade_app.services.DatabaseConnection;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.sql.SQLException;

public class StudentProfileController {
    @FXML
    private Label studentNameLabel;

    @FXML
    private Label studentSurnameLabel;

    @FXML
    private Label studentYearLabel;

    @FXML
    private Label studentDegreeLabel;

    @FXML
    private Button studentProfileButton;

    @FXML
    private Button studentGradesButton;

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
        GradeApp.setRoot("LogInPage");
    }

    @FXML
    public void initialize(){
        studentProfileButton.setFocusTraversable(true);
        studentGradesButton.setFocusTraversable(false);

        Student student = (Student) LoginUser.getUser();

        studentNameLabel.setText("Name: " + student.getName());
        studentSurnameLabel.setText("Surname: " + student.getSurname());
        studentYearLabel.setText("Year: " + String.valueOf(student.getYear()));

        try {
            DatabaseConnection database = new DatabaseConnection();

            studentDegreeLabel.setText("Course: " + database.getCoursePerStudent(student.getUserID()));
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
}