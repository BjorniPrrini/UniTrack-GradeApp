package com.grade_app.controllers;

import com.grade_app.GradeApp;

import com.grade_app.model.Assessment;
import com.grade_app.model.LoginUser;
import com.grade_app.model.Subject;
import com.grade_app.model.User;
import com.grade_app.services.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class StudentGradesController {
    @FXML
    private Button studentProfileButton;

    @FXML
    private Button studentGradesButton;

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
    public void initialize() {
        studentProfileButton.setFocusTraversable(false);
        studentGradesButton.setFocusTraversable(true);

        try {
            DatabaseConnection database = new DatabaseConnection();

            loadStudentGrades();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStudentGrades() throws SQLException {
        DatabaseConnection database = new DatabaseConnection();

        studentCenterVbox.getChildren().clear();

        User loggedInUser = LoginUser.getUser();

        if(loggedInUser == null){
            return;
        }

        int studentID = loggedInUser.getUserID();

        try {
            List<Subject> subjects = database.getSubjectsForStudent(studentID);

            for(Subject subject : subjects){
                VBox subjectCard = new VBox(5);

                subjectCard.setStyle(
                        "-fx-border-color: #2196f3;" +
                                "-fx-border-width: 2;" +
                                "-fx-padding: 10;" +
                                "-fx-background-color: #e3f2fd;"
                );

                Label subjectLabel = createBorderedLabel(subject.getName());

                subjectLabel.setStyle(subjectLabel.getStyle() + "-fx-font-size: 18px; -fx-font-weight: bold;");
                subjectCard.getChildren().add(subjectLabel);

                Label assessmentsHeader = createBorderedLabel("Assessments:");
                assessmentsHeader.setStyle(assessmentsHeader.getStyle() + "-fx-font-weight: bold;");
                subjectCard.getChildren().add(assessmentsHeader);

                List<Assessment> assessments = database.getAssessmentsForStudent(studentID, subject.getId());

                double totalWeightedGrade = 0;
                int totalWeight = 0;

                for(Assessment assessment : assessments){
                    int grade = assessment.getGradeValue();
                    int weight = assessment.getWeight();

                    double percentage = 0;

                    if(weight > 0){
                        percentage = ((double) grade / weight) * 100;
                    }

                    Label assessmentLabel = createBorderedLabel(
                            assessment.getName() + " - " + Math.round(percentage) + " / 100"
                    );
                    subjectCard.getChildren().add(assessmentLabel);

                    totalWeightedGrade += grade;
                    totalWeight += weight;
                }

                double finalPercentage = 0;
                if (totalWeight > 0) {
                    finalPercentage = (totalWeightedGrade / totalWeight) * 100;
                }

                Label finalLabel = createBorderedLabel(
                        "Final Grade: " + Math.round(finalPercentage) + " / 100"
                );
                finalLabel.setStyle(finalLabel.getStyle() + "-fx-font-weight: bold; -fx-font-size: 16px;");
                subjectCard.getChildren().add(finalLabel);

                studentCenterVbox.getChildren().add(subjectCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load grades: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private Label createBorderedLabel(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-border-color: black;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 5;" +
                        "-fx-background-color: #f0f0f0;"
        );

        return label;
    }
}