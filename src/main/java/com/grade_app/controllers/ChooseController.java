package com.grade_app.controllers;

import com.grade_app.GradeApp;
import javafx.fxml.FXML;

import java.io.IOException;

public class ChooseController {
    @FXML
    public void studentLogin() throws IOException {
        GradeApp.setRoot("LoginStudent");
    }

    @FXML
    public void teacherLogin() throws IOException{
        GradeApp.setRoot("LoginTeacher");
    }

    @FXML
    public void adminLogin() throws IOException{
        GradeApp.setRoot("LoginAdmin");
    }
}