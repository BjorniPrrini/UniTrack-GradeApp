package com.grade_app.controllers;

import com.grade_app.GradeApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public class AdminInsertController {
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
}