package com.grade_app.controllers;

import com.grade_app.GradeApp;
import com.grade_app.model.LoginUser;
import com.grade_app.model.StudentGrade;
import com.grade_app.model.StudentRow;
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
    private TableView<StudentRow> studentGradeTable;

    @FXML
    private Button submitButton;

    @FXML
    private Button finalizeButton;

    @FXML
    public void teacherGrades() throws IOException{
        GradeApp.setRoot("TeacherGrades");
    }

    @FXML
    public void teacherProfile() throws IOException{
        GradeApp.setRoot("TeacherProfile");
    }

    @FXML
    public void changePassword() throws IOException {
        GradeApp.setRoot("PasswordChanger");
    }

    @FXML
    public void logOut() throws IOException{
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to log out?");

        if(confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK){
            GradeApp.setRoot("LogInPage");
        }
    }

    private ObservableList<StudentRow> studentRows;
    private List<String> assessmentNames;
    private Map<String, Integer> assessmentWeights;
    private boolean gradesFinalized;
    private boolean isTableListenerAdded = false;

    @FXML
    public void initialize(){
        studentGradeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadSubjects();

        setupEventHandlers();
    }

    private void loadSubjects(){
        try {
            DatabaseConnection database = new DatabaseConnection();

            List<String> subjects = database.getSubjectInTeacher(LoginUser.getUser().getUserID());

            subjectComboBox.getItems().addAll(subjects);
        } catch (SQLException e) {
            showError("Failed to load subjects", "Subjects could not be loaded!");
        }
    }

    private void setupEventHandlers() {
        subjectComboBox.setOnAction(e -> loadGradesForSelectedSubject());
        submitButton.setOnAction(e -> saveGrades());
        finalizeButton.setOnAction(e -> finalized());
    }

    private void loadGradesForSelectedSubject() {
        String subject = subjectComboBox.getValue();

        if(subject == null || subject.trim().isEmpty()){
            return;
        }

        try {
            DatabaseConnection database = new DatabaseConnection();

            int subjectID = database.getSubjectIDByName(subject);

            gradesFinalized = database.areGradesFinalized(subjectID);
            assessmentWeights = database.getAssessmentWeightsForSubject(subjectID);

            List<StudentGrade> grades = database.getStudentGradesForSubject(subjectID);

            if(grades.isEmpty()){
                clearTable();

                showInfo("No Data", "No students or assessments found for this subject.");

                return;
            }

            buildTable(grades);

            updateUIState();
        } catch (Exception e) {
            showError("Error Loading Grades", "Grades could not be loaded!");
        }
    }

    private void buildTable(List<StudentGrade> grades){
        assessmentNames = extractAssessmentNames(grades);

        Map<Integer, StudentRow> studentMap = groupByStudent(grades);

        studentRows = FXCollections.observableArrayList(studentMap.values());

        for(StudentRow row : studentRows){
            row.calculateAverage(assessmentNames, assessmentWeights);
        }

        createColumns();

        studentGradeTable.setItems(studentRows);

        setupAutoResize();
    }

    private List<String> extractAssessmentNames(List<StudentGrade> grades){
        Set<String> unique = new LinkedHashSet<>();

        for(StudentGrade g : grades){
            unique.add(g.getAssessmentName());
        }

        return new ArrayList<>(unique);
    }

    private Map<Integer, StudentRow> groupByStudent(List<StudentGrade> grades){
        Map<Integer, StudentRow> map = new LinkedHashMap<>();

        for(StudentGrade g : grades){
            StudentRow row = map.computeIfAbsent(g.getStudentID(), id -> new StudentRow(id, g.getStudentName()));
            row.addGrade(g.getAssessmentName(), g.getGradeValue(), g.getGradeID());
        }

        return map;
    }

    private static final double NAME_COLUMN_PERCENT = 0.20;
    private static final double FINAL_COLUMN_PERCENT = 0.15;
    private static final double MIN_COLUMN_WIDTH = 90.0;
    private static final int MIN_GRADE = 0;
    private static final int MAX_GRADE = 100;

    private void createColumns(){
        studentGradeTable.getColumns().clear();

        TableColumn<StudentRow, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        nameCol.setEditable(false);
        nameCol.setMinWidth(MIN_COLUMN_WIDTH);
        studentGradeTable.getColumns().add(nameCol);

        for(String assessment : assessmentNames){
            TableColumn<StudentRow, Integer> col = new TableColumn<>(assessment);
            col.setCellValueFactory(data -> {
                Integer grade = data.getValue().getGrades().get(assessment);
                return new SimpleIntegerProperty(grade != null ? grade : 0).asObject();
            });
            col.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            col.setOnEditCommit(this::handleGradeEdit);
            col.setEditable(!gradesFinalized);
            col.setMinWidth(MIN_COLUMN_WIDTH);

            studentGradeTable.getColumns().add(col);
        }

        TableColumn<StudentRow, String> finalCol = new TableColumn<>("Final Grade");
        finalCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f", data.getValue().getFinalGrade()))
        );
        finalCol.setEditable(false);
        finalCol.setMinWidth(MIN_COLUMN_WIDTH);

        studentGradeTable.getColumns().add(finalCol);
    }

    private void handleGradeEdit(TableColumn.CellEditEvent<StudentRow, Integer> event){
        if(gradesFinalized){
            studentGradeTable.refresh();

            return;
        }

        int newGrade = event.getNewValue();

        if(newGrade < MIN_GRADE || newGrade > MAX_GRADE){
            showError("Invalid Grade", "Grade must be between " + MIN_GRADE + " and " + MAX_GRADE);

            studentGradeTable.refresh();

            return;
        }

        String assessment = event.getTableColumn().getText();
        StudentRow row = event.getRowValue();
        row.getGrades().put(assessment, newGrade);
        row.calculateAverage(assessmentNames, assessmentWeights);

        studentGradeTable.refresh();
    }

    private void setupAutoResize(){
        if(isTableListenerAdded){
            return;
        }

        studentGradeTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double tableWidth = newWidth.doubleValue();
            int numCols = studentGradeTable.getColumns().size();

            if (numCols < 2){
                return;
            }

            double nameWidth = tableWidth * NAME_COLUMN_PERCENT;
            double finalWidth = tableWidth * FINAL_COLUMN_PERCENT;
            double assessmentTotal = tableWidth - nameWidth - finalWidth;
            double assessmentWidth = assessmentNames.isEmpty() ? 0 : assessmentTotal / assessmentNames.size();

            for(int i = 0; i < numCols; i++){
                TableColumn<?, ?> col = studentGradeTable.getColumns().get(i);

                if(i == 0){
                    col.setPrefWidth(Math.max(MIN_COLUMN_WIDTH, nameWidth));
                }else if(i == numCols - 1){
                    col.setPrefWidth(Math.max(MIN_COLUMN_WIDTH, finalWidth));
                }else{
                    col.setPrefWidth(Math.max(MIN_COLUMN_WIDTH, assessmentWidth));
                }
            }
        });

        isTableListenerAdded = true;
    }

    private void saveGrades(){
        if(gradesFinalized){
            showWarning("Grades Finalized", "Cannot edit finalized grades.");

            return;
        }

        try {
            DatabaseConnection database = new DatabaseConnection();

            int updatedCount = 0;

            for(StudentRow row : studentRows){
                for(String assessment : assessmentNames){
                    Integer gradeID = row.getGradeIDs().get(assessment);
                    Integer gradeValue = row.getGrades().get(assessment);

                    if(gradeID != null && gradeValue != null){
                        database.updateGrade(gradeID, gradeValue);

                        updatedCount++;
                    }
                }
            }

            showInfo("Success", "Updated " + updatedCount + " grade(s) successfully.");

            loadGradesForSelectedSubject();

        } catch (Exception e) {
            showError("Error Saving Grades", "Grades could not  be saved!");
        }
    }

    private void finalized(){
        String subject = subjectComboBox.getValue();

        if(subject == null || subject.trim().isEmpty()){
            showWarning("No Subject Selected", "Please select a subject first.");

            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Finalize Grades");
        confirm.setHeaderText("Finalize grades for " + subject + "?");
        confirm.setContentText("This action cannot be undone. Grades will be locked.");

        if(confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK){
            return;
        }

        try {
            DatabaseConnection database = new DatabaseConnection();

            int subjectID = database.getSubjectIDByName(subject);

            database.finalizeGradesForSubject(subjectID);

            showInfo("Success", "Grades have been finalized.");

            loadGradesForSelectedSubject();
        } catch (Exception e) {
            showError("Error Finalizing Grades", "Grades couldn't be finalized!");
        }
    }

    private void clearTable(){
        studentGradeTable.getColumns().clear();
        studentGradeTable.getItems().clear();
    }

    private void updateUIState(){
        studentGradeTable.setEditable(!gradesFinalized);
        submitButton.setDisable(gradesFinalized);
        finalizeButton.setDisable(gradesFinalized);
    }

    private void showError(String title, String message){
        showAlert(title, message, Alert.AlertType.ERROR);
    }

    private void showWarning(String title, String message){
        showAlert(title, message, Alert.AlertType.WARNING);
    }

    private void showInfo(String title, String message){
        showAlert(title, message, Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}