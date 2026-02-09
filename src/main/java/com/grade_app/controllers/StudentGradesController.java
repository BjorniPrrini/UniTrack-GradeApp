package com.grade_app.controllers;

import com.grade_app.GradeApp;
import com.grade_app.model.Assessment;
import com.grade_app.model.LoginUser;
import com.grade_app.model.Student;
import com.grade_app.model.Subject;
import com.grade_app.model.User;
import com.grade_app.services.DatabaseConnection;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class StudentGradesController {
    @FXML
    private VBox studentCenterVbox;

    @FXML
    public void openStudentGrades() throws IOException {
        GradeApp.setRoot("StudentGrades");
    }

    @FXML
    public void openStudentProfile() throws IOException {
        GradeApp.setRoot("StudentProfile");
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
        alert.setContentText("Are you sure you want to log out?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            GradeApp.setRoot("LogInPage");
        }
    }

    @FXML
    public void initialize(){
        loadStudentGrades();
    }

    private void loadStudentGrades(){
        User loggedInUser = LoginUser.getUser();

        if(loggedInUser == null){
            showError("Error", "No user logged in.");

            return;
        }

        if(!(loggedInUser instanceof Student)){
            showError("Error", "Logged in user is not a student.");

            return;
        }

        Student student = (Student) loggedInUser;

        int studentID = student.getUserID();
        int studentYear = student.getYear();

        try {
            DatabaseConnection database = new DatabaseConnection();

            studentCenterVbox.getChildren().clear();

            List<Subject> subjects = database.getSubjectsForStudent(studentID, studentYear);

            if(subjects.isEmpty()){
                Label noDataLabel = new Label("No subjects found for your current year.");

                noDataLabel.setStyle("-fx-font-size: 16px; -fx-padding: 20;");

                studentCenterVbox.getChildren().add(noDataLabel);

                return;
            }

            for(Subject subject : subjects){
                VBox subjectCard = createSubjectCard(subject, studentID, database);

                studentCenterVbox.getChildren().add(subjectCard);
            }

        } catch (SQLException e) {
            showError("Database Error", "Failed to load grades!");

            e.printStackTrace();
        }
    }

    private VBox createSubjectCard(Subject subject, int studentID, DatabaseConnection database) throws SQLException{
        VBox subjectCard = new VBox(8);
        subjectCard.setStyle("-fx-border-color: #c0875c; -fx-border-width: 2; -fx-padding: 15; -fx-background-color: #E9C68A; -fx-background-radius: 5; -fx-border-radius: 5;");

        Label subjectLabel = createLabel(subject.getName());
        subjectLabel.setStyle(subjectLabel.getStyle() + "-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: #c0875c; -fx-text-fill: white; -fx-border-color: white;");
        subjectCard.getChildren().add(subjectLabel);

        Label assessmentsHeader = createLabel("Assessments:");
        assessmentsHeader.setStyle(assessmentsHeader.getStyle() + "-fx-font-weight: bold; -fx-background-color: white; -fx-border-color:#c0875c; -fx-border-width: 2;");
        subjectCard.getChildren().add(assessmentsHeader);

        List<Assessment> assessments = database.getAssessmentsForStudent(studentID, subject.getId());

        double totalWeightedGrade = 0;
        int totalWeight = 0;

        for(Assessment assessment : assessments){
            int grade = assessment.getGradeValue();
            int weight = assessment.getWeight();

            Label assessmentLabel = createLabel(assessment.getName() + " (Weight: " + weight + "%) - " + grade + " / 100");
            subjectCard.getChildren().add(assessmentLabel);

            totalWeightedGrade += (grade * weight);
            totalWeight += weight;
        }

        double finalGrade = totalWeight > 0 ? totalWeightedGrade / totalWeight : 0.0;

        Label finalLabel = createFinalGradeLabel(finalGrade);

        subjectCard.getChildren().add(finalLabel);

        return subjectCard;
    }

    private Label createLabel(String text){
        Label label = new Label(text);

        label.setStyle("-fx-border-color: #c0875c; -fx-border-width: 1; -fx-padding: 8; -fx-background-color: white; -fx-border-radius: 3; -fx-background-radius: 3;");

        return label;
    }

    private Label createFinalGradeLabel(double finalGrade){
        Label label = createLabel("Final Grade: " + String.format("%.2f", finalGrade) + " / 100");

        String color = getGradeColor(finalGrade);

        label.setStyle(label.getStyle() + "-fx-font-weight: bold; -fx-font-size: 16px; -fx-background-color: " + color + "; -fx-text-fill: white; -fx-border-color: " + darkenColor(color) + ";");

        return label;
    }

    private static final double excellent = 90.0;
    private static final double good = 70.0;
    private static final double pass = 60.0;
    private static final double fail = 50.0;

    private String getGradeColor(double grade){
        if(grade >= excellent){
            return "#4caf50";
        }else if(grade >= good){
            return "#2196f3";
        }else if(grade >= pass){
            return "#ff9800";
        }else if(grade < fail){
            return "#f44336";
        }else{
            return "#ff9800";
        }
    }

    private String darkenColor(String hexColor){
        return switch (hexColor) {
            case "#4caf50" -> "#388e3c";
            case "#2196f3" -> "#1976d2";
            case "#ff9800" -> "#f57c00";
            case "#f44336" -> "#d32f2f";
            default -> "#757575";
        };
    }

    private void showError(String title, String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}