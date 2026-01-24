package com.grade_app.controllers;

import com.grade_app.GradeApp;
import com.grade_app.model.LoginUser;
import com.grade_app.model.StudentGrade;
import com.grade_app.services.DatabaseConnection;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class TeacherGradesController {
    @FXML
    private Button teacherGradesButton;

    @FXML
    private Button teacherProfileButton;

    @FXML
    private ComboBox<String> subjectComboBox;

    @FXML
    private TableView<Map<String, Object>> studentGradeTable;

    @FXML
    private Button submitButton;

    @FXML
    private Button finalizeButton;

    private ObservableList<Map<String, Object>> tableData;
    private List<String> assessmentColumns;
    private boolean gradesFinalized = false;

    @FXML
    public void teacherGrades() throws IOException{
        GradeApp.setRoot("TeacherGrades");
    }

    @FXML
    public void teacherProfile() throws IOException{
        GradeApp.setRoot("TeacherProfile");
    }

    @FXML
    public void logOut() throws IOException{
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
        teacherProfileButton.setFocusTraversable(false);
        teacherGradesButton.setFocusTraversable(true);

        try {
            DatabaseConnection database = new DatabaseConnection();

            subjectComboBox.getItems().addAll(database.getSubjectInTeacher(LoginUser.getUser().getUserID()));

            subjectComboBox.setOnAction(event -> loadGradesTable());

            submitButton.setOnAction(event -> submitGrades());
            finalizeButton.setOnAction(event -> finalizeGrades());

        } catch (SQLException e) {
            e.printStackTrace();

            showAlert("Database Error", "Failed to connect to database", Alert.AlertType.ERROR);
        }
    }

    private void loadGradesTable(){
        String selectedSubject = subjectComboBox.getValue();

        if(selectedSubject == null || selectedSubject.isEmpty()){
            return;
        }

        try {

            DatabaseConnection database = new DatabaseConnection();

            int subjectID = database.getSubjectIDByName(selectedSubject);

            if(subjectID == -1){
                showAlert("Error", "Subject not found", Alert.AlertType.ERROR);

                return;
            }

            gradesFinalized = database.areGradesFinalized(subjectID);

            List<StudentGrade> studentGrades = database.getStudentGradesForSubject(subjectID);

            if(studentGrades.isEmpty()){
                studentGradeTable.getItems().clear();
                studentGradeTable.getColumns().clear();

                showAlert("No Data", "No students enrolled in this subject", Alert.AlertType.INFORMATION);

                return;
            }

            buildTableStructure(studentGrades);
            populateTableData(studentGrades);

            studentGradeTable.setEditable(!gradesFinalized);
            submitButton.setDisable(gradesFinalized);
            finalizeButton.setDisable(gradesFinalized);

            if(gradesFinalized){
                showAlert("Information", "Grades for this subject have been finalized and cannot be edited", Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            e.printStackTrace();

            showAlert("Error", "Failed to load grades: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void buildTableStructure(List<StudentGrade> studentGrades){
        studentGradeTable.getColumns().clear();
        assessmentColumns = new ArrayList<>();

        Set<String> uniqueAssessments = new LinkedHashSet<>();

        for(StudentGrade sg : studentGrades){
            uniqueAssessments.add(sg.getAssessmentName());
        }

        double tableWidth = 700.0;
        double nameColumnWidth = 180.0;
        double avgColumnWidth = 90.0;
        int numAssessments = uniqueAssessments.size();
        double assessmentColumnWidth = (tableWidth - nameColumnWidth - avgColumnWidth) / Math.max(numAssessments, 1);

        assessmentColumnWidth = Math.max(assessmentColumnWidth, 100.0);

        TableColumn<Map<String, Object>, String> nameColumn = new TableColumn<>("Student Name");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty((String) cellData.getValue().get("studentName")));
        nameColumn.setMinWidth(150);
        nameColumn.setPrefWidth(nameColumnWidth);
        nameColumn.setMaxWidth(250);
        nameColumn.setEditable(false);
        studentGradeTable.getColumns().add(nameColumn);

        for(String assessmentName : uniqueAssessments){
            assessmentColumns.add(assessmentName);

            TableColumn<Map<String, Object>, Integer> assessmentColumn = new TableColumn<>(assessmentName);
            assessmentColumn.setCellValueFactory(cellData -> {
                Object value = cellData.getValue().get(assessmentName);

                return new SimpleIntegerProperty((Integer) value).asObject();
            });

            assessmentColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            assessmentColumn.setOnEditCommit(event -> {
                if(gradesFinalized){
                    showAlert("Error", "Cannot edit finalized grades", Alert.AlertType.ERROR);
                    loadGradesTable();

                    return;
                }

                Map<String, Object> row = event.getRowValue();
                int newValue = event.getNewValue();

                if(newValue < 0 || newValue > 100){
                    showAlert("Invalid Grade", "Grade must be between 0 and 100", Alert.AlertType.ERROR);
                    loadGradesTable();

                    return;
                }

                row.put(assessmentName, newValue);
            });

            assessmentColumn.setMinWidth(80);
            assessmentColumn.setPrefWidth(assessmentColumnWidth);
            assessmentColumn.setMaxWidth(200);
            assessmentColumn.setEditable(true);
            studentGradeTable.getColumns().add(assessmentColumn);
        }

        TableColumn<Map<String, Object>, String> avgColumn = new TableColumn<>("Average");
        avgColumn.setCellValueFactory(cellData -> {
            double avg = (Double) cellData.getValue().get("average");
            return new SimpleStringProperty(String.format("%.2f", avg));
        });
        avgColumn.setMinWidth(80);
        avgColumn.setPrefWidth(avgColumnWidth);
        avgColumn.setMaxWidth(120);
        avgColumn.setEditable(false);
        studentGradeTable.getColumns().add(avgColumn);
    }

    private void populateTableData(List<StudentGrade> studentGrades){
        tableData = FXCollections.observableArrayList();
        Map<String, Map<String, Object>> studentRows = new LinkedHashMap<>();

        for(StudentGrade sg : studentGrades){
            String studentKey = sg.getStudentID() + "_" + sg.getStudentName();

            studentRows.putIfAbsent(studentKey, new HashMap<>());
            Map<String, Object> row = studentRows.get(studentKey);

            row.put("studentID", sg.getStudentID());
            row.put("studentName", sg.getStudentName());
            row.put(sg.getAssessmentName(), sg.getGradeValue());
            row.put(sg.getAssessmentName() + "_gradeID", sg.getGradeID());
        }

        for(Map<String, Object> row : studentRows.values()){
            int sum = 0;
            int count = 0;

            for(String assessment : assessmentColumns){
                if(row.containsKey(assessment)){
                    sum += (Integer) row.get(assessment);
                    count++;
                }
            }

            double average = count > 0 ? (double) sum / count : 0.0;
            row.put("average", average);

            tableData.add(row);
        }

        studentGradeTable.setItems(tableData);
    }

    private void submitGrades(){
        if(gradesFinalized){
            showAlert("Error", "Cannot submit finalized grades", Alert.AlertType.ERROR);

            return;
        }

        try {
            DatabaseConnection database = new DatabaseConnection();

            int updatedCount = 0;

            for(Map<String, Object> row : tableData){
                for(String assessment : assessmentColumns){
                    if(row.containsKey(assessment + "_gradeID")){
                        int gradeID = (Integer) row.get(assessment + "_gradeID");
                        int gradeValue = (Integer) row.get(assessment);

                        if(database.updateGrade(gradeID, gradeValue)){
                            updatedCount++;
                        }
                    }
                }
            }

            if(updatedCount > 0){
                showAlert("Success", "Grades submitted successfully! (" + updatedCount + " grades updated)", Alert.AlertType.INFORMATION);
                loadGradesTable();
            }else{
                showAlert("Information", "No grades were updated", Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            e.printStackTrace();

            showAlert("Error", "Failed to submit grades: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void finalizeGrades(){
        if(gradesFinalized){
            showAlert("Error", "Grades are already finalized", Alert.AlertType.ERROR);

            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Finalize Grades");
        confirmAlert.setHeaderText("Are you sure you want to finalize grades?");
        confirmAlert.setContentText("Once finalized, grades cannot be edited anymore!");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.OK){
            try {
                DatabaseConnection database = new DatabaseConnection();

                String selectedSubject = subjectComboBox.getValue();
                int subjectID = database.getSubjectIDByName(selectedSubject);

                if(database.finalizeGradesForSubject(subjectID)){
                    showAlert("Success", "Grades have been finalized successfully!", Alert.AlertType.INFORMATION);
                    loadGradesTable();
                }else{
                    showAlert("Error", "Failed to finalize grades", Alert.AlertType.ERROR);
                }

            } catch (Exception e) {
                e.printStackTrace();

                showAlert("Error", "Failed to finalize grades: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}