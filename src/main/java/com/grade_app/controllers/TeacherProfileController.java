package com.grade_app.controllers;

import com.grade_app.GradeApp;
import com.grade_app.model.LoginUser;
import com.grade_app.services.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TeacherProfileController {
    @FXML
    private Button teacherGradesButton;

    @FXML
    private Button teacherProfileButton;

    @FXML
    private Label nameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private VBox subjectsBox;

    @FXML
    public void teacherGrades() throws IOException {
        GradeApp.setRoot("TeacherGrades");
    }

    @FXML
    public void teacherProfile() throws IOException {
        GradeApp.setRoot("TeacherProfile");
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
        teacherProfileButton.setFocusTraversable(true);
        teacherGradesButton.setFocusTraversable(false);

        nameLabel.setText(LoginUser.getUser().getName() + " " + LoginUser.getUser().getSurname());
        emailLabel.setText(LoginUser.getUser().getEmail());

        try{
            DatabaseConnection database = new DatabaseConnection();

            List<String> subjectsInTeacher = database.getSubjectInTeacher(LoginUser.getUser().getUserID());

            subjectsBox.getChildren().clear();

            for(String subject : subjectsInTeacher){
                Label subjectLabel = new Label(subject);

                subjectsBox.getChildren().add(subjectLabel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}