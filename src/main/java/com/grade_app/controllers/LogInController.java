package com.grade_app.controllers;

import com.grade_app.GradeApp;
import javafx.fxml.FXML;

import java.io.IOException;

public class LogInController {
    @FXML
    public void openHomePage() throws  IOException{
        GradeApp.setRoot("AdminHomePage");
    }
}
