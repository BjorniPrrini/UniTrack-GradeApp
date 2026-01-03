package com.grade_app.controllers;

import com.grade_app.GradeApp;
import com.grade_app.model.Course;
import com.grade_app.model.Subject;
import com.grade_app.services.DatabaseConnection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class AdminSubjectsController {
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
    private ComboBox<Course> coursesComboBox;

    @FXML
    private ComboBox<Subject> subjectComboBox;

    @FXML
    private CheckBox finalCheckBox;

    @FXML
    private CheckBox quizCheckBox;

    @FXML
    private CheckBox midtermCheckBox;

    @FXML
    private CheckBox homeworkCheckBox;

    @FXML
    private CheckBox projectCheckBox;

    @FXML
    private TextField midtermField;

    @FXML
    private TextField finalField;

    @FXML
    private TextField quizField;

    @FXML
    private TextField homeworkField;

    @FXML
    private TextField projectField;

    @FXML
    private Label totalAssignedLabel;

    @FXML
    private Label remainingLabel;

    @FXML
    private Button submitButton;

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
        insertButton.setFocusTraversable(false);
        deleteButton.setFocusTraversable(false);
        assignButton.setFocusTraversable(false);
        logOutButton.setFocusTraversable(false);
        subjectButton.setFocusTraversable(true);

        DatabaseConnection database = new DatabaseConnection();

        coursesComboBox.getItems().addAll(database.getCourses());

        coursesComboBox.valueProperty().addListener((_, _, course) -> {
            subjectComboBox.getItems().clear();

            if(course != null){
                subjectComboBox.getItems().addAll(database.getSubjects(course.getId()));
            }
        });

        subjectComboBox.valueProperty().addListener((_, _, subject) -> {
            if(subject != null){
                Map<String, Integer> assessments = database.getSubjectAssessments(subject.getId());

                midtermCheckBox.setSelected(assessments.containsKey("Midterm Exam"));
                midtermField.setText(assessments.containsKey("Midterm Exam") ? assessments.get("Midterm Exam").toString() : "");

                finalCheckBox.setSelected(assessments.containsKey("Final Exam"));
                finalField.setText(assessments.containsKey("Final Exam") ? assessments.get("Final Exam").toString() : "");

                quizCheckBox.setSelected(assessments.containsKey("Quiz"));
                quizField.setText(assessments.containsKey("Quiz") ? assessments.get("Quiz").toString() : "");

                projectCheckBox.setSelected(assessments.containsKey("Project"));
                projectField.setText(assessments.containsKey("Project") ? assessments.get("Project").toString() : "");

                homeworkCheckBox.setSelected(assessments.containsKey("Homework"));
                homeworkField.setText(assessments.containsKey("Homework") ? assessments.get("Homework").toString() : "");
            }else{
                midtermCheckBox.setSelected(false);
                midtermField.clear();
                finalCheckBox.setSelected(false);
                finalField.clear();
                quizCheckBox.setSelected(false);
                quizField.clear();
                projectCheckBox.setSelected(false);
                projectField.clear();
                homeworkCheckBox.setSelected(false);
                homeworkField.clear();
            }
        });

        midtermField.disableProperty().bind(midtermCheckBox.selectedProperty().not());
        midtermCheckBox.selectedProperty().addListener((_, _, selected) -> {
            if(!selected){
                midtermField.clear();
            }
        });
        midtermField.textProperty().addListener((obs, oldText, newText) -> updatePercentages());

        finalField.disableProperty().bind(finalCheckBox.selectedProperty().not());
        finalCheckBox.selectedProperty().addListener((_, _, selected) -> {
            if(!selected){
                finalField.clear();
            }
        });
        finalField.textProperty().addListener((_, _, _) -> updatePercentages());

        quizField.disableProperty().bind(quizCheckBox.selectedProperty().not());
        quizCheckBox.selectedProperty().addListener((_, _, selected) -> {
            if(!selected){
                quizField.clear();
            }
        });
        quizField.textProperty().addListener((_, _, _) -> updatePercentages());

        projectField.disableProperty().bind(projectCheckBox.selectedProperty().not());
        projectCheckBox.selectedProperty().addListener((_, _, selected) -> {
            if(!selected){
                projectField.clear();
            }
        });
        projectField.textProperty().addListener((_, _, _) -> updatePercentages());

        homeworkField.disableProperty().bind(homeworkCheckBox.selectedProperty().not());
        homeworkCheckBox.selectedProperty().addListener((_, _, selected) -> {
            if(!selected){
                homeworkField.clear();
            }
        });
        homeworkField.textProperty().addListener((_, _, _) -> updatePercentages());
    }

    private void updatePercentages() {
        int midterm = getPercentage(midtermCheckBox, midtermField);
        int fin = getPercentage(finalCheckBox, finalField);
        int quiz = getPercentage(quizCheckBox, quizField);
        int project = getPercentage(projectCheckBox, projectField);
        int homework = getPercentage(homeworkCheckBox, homeworkField);

        int total = midterm + fin + quiz + project + homework;
        int remaining = 100 - total;

        totalAssignedLabel.setText("Total Assigned: " + total);
        remainingLabel.setText("Remaining: " + remaining);
    }

    private int getPercentage(CheckBox checkBox, TextField field){
        if(!checkBox.isSelected()){
            return 0;
        }

        String text = field.getText();

        if(text == null || text.isBlank()){
            return 0;
        }

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @FXML
    public void submit(){
        submitButton.setDisable(true);

        boolean success = true;
        try {
            DatabaseConnection database = new DatabaseConnection();

            int subjectID = subjectComboBox.getValue().getId();

            if(midtermCheckBox.isSelected()){
                int weight = Integer.parseInt(midtermField.getText());

                success = success && database.addOrUpdateSubjectAssessment(subjectID, "Midterm Exam", weight, true);
            }else{
                success = success && database.setAssessmentActive(subjectID, "Midterm Exam", false);
            }

            if(finalCheckBox.isSelected()){
                int weight = Integer.parseInt(finalField.getText());

                success = success && database.addOrUpdateSubjectAssessment(subjectID, "Final Exam", weight, true);
            }else{
                success = success && database.setAssessmentActive(subjectID, "Final Exam", false);
            }

            if(quizCheckBox.isSelected()){
                int weight = Integer.parseInt(quizField.getText());

                success = success && database.addOrUpdateSubjectAssessment(subjectID, "Quiz", weight, true);
            }else{
                success = success && database.setAssessmentActive(subjectID, "Quiz", false);
            }

            if(projectCheckBox.isSelected()){
                int weight = Integer.parseInt(projectField.getText());

                success = success && database.addOrUpdateSubjectAssessment(subjectID, "Project", weight, true);
            }else{
                success = success && database.setAssessmentActive(subjectID, "Project", false);
            }

            if(homeworkCheckBox.isSelected()){
                int weight = Integer.parseInt(homeworkField.getText());

                success = success && database.addOrUpdateSubjectAssessment(subjectID, "Homework", weight, true);
            }else{
                success = success && database.setAssessmentActive(subjectID, "Homework", false);
            }

            if(success){
                showSuccessAlert("Assessment successful", "Assessment was places successfully for this subject!");
            }else{
                showAlert("Error" ,"Assessment was not successfully placed!");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            submitButton.setDisable(false);
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