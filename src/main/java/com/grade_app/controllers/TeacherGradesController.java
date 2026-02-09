package com.grade_app.controllers;

import com.grade_app.GradeApp;
import com.grade_app.model.LoginUser;
import com.grade_app.model.StudentGrade;
import com.grade_app.model.StudentRow;
import com.grade_app.model.Assessment;
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
    private ComboBox<String> subjectComboBox;

    @FXML
    private TableView<StudentRow> studentGradeTable;

    @FXML
    private Button submitButton;

    @FXML
    private Button finalizeButton;

    private ObservableList<StudentRow> studentRows;
    private List<String> assessmentNames = new ArrayList<>();
    private boolean gradesFinalized = false;

    private static final int MIN_GRADE = 0;
    private static final int MAX_GRADE = 100;
    private static final int MIN_COLUMN_WIDTH = 80;

    @FXML
    public void teacherGrades() throws IOException {
        GradeApp.setRoot("TeacherGrades");
    }

    @FXML
    public void teacherProfile() throws IOException {
        GradeApp.setRoot("TeacherProfile");
    }

    @FXML
    public void changePassword() throws IOException {
        GradeApp.setRoot("PasswordChanger");
    }

    @FXML
    public void logOut() throws IOException {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to log out?", ButtonType.OK, ButtonType.CANCEL);

        if(confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK){
            GradeApp.setRoot("LogInPage");
        }
    }

    @FXML
    public void initialize() {
        studentGradeTable.setEditable(true);

        loadSubjects();

        subjectComboBox.setOnAction(e -> loadGrades());
        submitButton.setOnAction(e -> saveGrades());
        finalizeButton.setOnAction(e -> finalizeGrades());
    }

    private void loadSubjects() {
        try {
            DatabaseConnection database = new DatabaseConnection();

            subjectComboBox.getItems().addAll(database.getSubjectInTeacher(LoginUser.getUser().getUserID()));
        } catch (SQLException e) {
            showError("Load Error", "Could not load subjects.");
        }
    }

    private void loadGrades() {
        String subject = subjectComboBox.getValue();

        if(subject == null){
            return;
        }

        try {
            DatabaseConnection database = new DatabaseConnection();

            int subjectID = database.getSubjectIDByName(subject);

            gradesFinalized = database.areGradesFinalized(subjectID);

            List<StudentGrade> grades = database.getStudentGradesForSubject(subjectID);

            if(grades.isEmpty()){
                clearTable();

                showInfo("No Data", "No grades found for this subject.");

                return;
            }

            buildTable(grades, subjectID);

            updateUI();
        } catch (Exception e) {
            showError("Load Error", "Failed to load grades.");

            e.printStackTrace();
        }
    }

    private void buildTable(List<StudentGrade> grades, int subjectID) throws SQLException {
        DatabaseConnection database = new DatabaseConnection();

        List<Assessment> assessmentList = database.getAssessmentWeightsForSubject(subjectID);

        List<StudentRow> rows = new ArrayList<>();

        assessmentNames.clear();

        for(Assessment assessment : assessmentList){
            String assessmentName = assessment.getName();

            if(!assessmentNames.contains(assessmentName)){
                assessmentNames.add(assessmentName);
            }
        }

        for(StudentGrade grade : grades){
            int studentId = grade.getStudentID();
            String studentName = grade.getStudentName();

            StudentRow studentRow = null;

            for(StudentRow row : rows){
                if(row.getStudentID() == studentId){
                    studentRow = row;

                    break;
                }
            }

            if(studentRow == null){
                studentRow = new StudentRow(studentId, studentName);

                rows.add(studentRow);
            }

            int weight = 0;
            int subjectAssessmentID = grade.getSubjectAssessmentID();

            for(Assessment assessment : assessmentList){
                if(assessment.getSubjectAssessmentID() == subjectAssessmentID){
                    weight = assessment.getWeight();

                    break;
                }
            }

            studentRow.addGrade(grade.getAssessmentName(), grade.getGradeValue(), grade.getGradeID(), weight);
        }

        for(StudentRow row : rows){
            row.calculateFinalGrade();
        }

        studentRows = FXCollections.observableArrayList(rows);

        createColumns();

        studentGradeTable.setItems(studentRows);

        adjustColumnWidths();
    }

    private void createColumns() {
        studentGradeTable.getColumns().clear();

        TableColumn<StudentRow, String> nameCol = new TableColumn<>("Student");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentName()));
        nameCol.setEditable(false);
        nameCol.setMinWidth(MIN_COLUMN_WIDTH);
        studentGradeTable.getColumns().add(nameCol);

        for(String assessment : assessmentNames){
            TableColumn<StudentRow, Integer> col = new TableColumn<>(assessment);

            col.setCellValueFactory(d -> {
                int gradeValue = d.getValue().getGradeForAssessment(assessment);

                return new SimpleIntegerProperty(gradeValue).asObject();
            });

            col.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            col.setEditable(!gradesFinalized);
            col.setMinWidth(MIN_COLUMN_WIDTH);

            col.setOnEditCommit(e -> {
                int newVal = e.getNewValue();

                if(newVal < MIN_GRADE || newVal > MAX_GRADE){
                    showWarning("Invalid Grade", "Grade must be 0–100");

                    studentGradeTable.refresh();

                    return;
                }

                e.getRowValue().updateGrade(assessment, newVal);

                studentGradeTable.refresh();
            });

            studentGradeTable.getColumns().add(col);
        }

        TableColumn<StudentRow, String> finalCol = new TableColumn<>("Final");
        finalCol.setCellValueFactory(d ->  new SimpleStringProperty(String.format("%.2f", d.getValue().getFinalGrade())));
        finalCol.setEditable(false);
        finalCol.setMinWidth(MIN_COLUMN_WIDTH);
        studentGradeTable.getColumns().add(finalCol);

        TableColumn<StudentRow, String> gradeLetterCol = new TableColumn<>("Grade");
        gradeLetterCol.setCellValueFactory(e ->  new SimpleStringProperty(e.getValue().getLetterGrade()));
        gradeLetterCol.setEditable(false);
        gradeLetterCol.setMinWidth(MIN_COLUMN_WIDTH);
        studentGradeTable.getColumns().add(gradeLetterCol);

        studentGradeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void adjustColumnWidths() {
        int totalColumns = studentGradeTable.getColumns().size();

        if(totalColumns == 0){
            return;
        }

        double tableWidth = studentGradeTable.getWidth();

        if(tableWidth <= 0){
            tableWidth = 800;
        }

        double nameColumnWidth = Math.max(MIN_COLUMN_WIDTH, tableWidth * 0.25);
        double finalColumnWidth = Math.max(MIN_COLUMN_WIDTH, tableWidth * 0.15);

        int assessmentColumns = totalColumns - 2;
        double remainingWidth = tableWidth - nameColumnWidth - finalColumnWidth;

        if(assessmentColumns > 0){
            double assessmentColumnWidth = Math.max(MIN_COLUMN_WIDTH, remainingWidth / assessmentColumns);

            for(int i = 0; i < totalColumns; i++){
                TableColumn<?, ?> column = studentGradeTable.getColumns().get(i);

                if(i == 0){
                    column.setPrefWidth(nameColumnWidth);
                }else if(i == totalColumns - 1){
                    column.setPrefWidth(finalColumnWidth);
                }else{
                    column.setPrefWidth(assessmentColumnWidth);
                }
            }
        }

        studentGradeTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            if(newWidth.doubleValue() > 0){
                adjustColumnWidthsOnResize(newWidth.doubleValue());
            }
        });
    }

    private void adjustColumnWidthsOnResize(double newWidth) {
        int totalColumns = studentGradeTable.getColumns().size();

        if(totalColumns == 0){
            return;
        }

        double nameColumnWidth = Math.max(MIN_COLUMN_WIDTH, newWidth * 0.25);
        double finalColumnWidth = Math.max(MIN_COLUMN_WIDTH, newWidth * 0.15);

        int assessmentColumns = totalColumns - 2;
        double remainingWidth = newWidth - nameColumnWidth - finalColumnWidth;

        if(assessmentColumns > 0 && remainingWidth > 0){
            double assessmentColumnWidth = Math.max(MIN_COLUMN_WIDTH, remainingWidth / assessmentColumns);

            for(int i = 0; i < totalColumns; i++){
                TableColumn<?, ?> column = studentGradeTable.getColumns().get(i);

                if(i == 0){
                    column.setPrefWidth(nameColumnWidth);
                }else if(i == totalColumns - 1){
                    column.setPrefWidth(finalColumnWidth);
                }else{
                    column.setPrefWidth(assessmentColumnWidth);
                }
            }
        }
    }

    @FXML
    private void saveGrades() {
        if(gradesFinalized){
            return;
        }

        try {
            DatabaseConnection database = new DatabaseConnection();

            int count = 0;

            for(StudentRow row : studentRows){
                for(StudentRow.GradeData grade : row.getGrades()){
                    database.updateGrade(grade.gradeID(), grade.gradeValue());

                    count++;
                }
            }

            showInfo("Saved", count + " grades updated.");

            loadGrades();
        } catch (Exception e) {
            showError("Save Error", "Could not save grades.");

            e.printStackTrace();
        }
    }

    @FXML
    private void finalizeGrades() {
        String subject = subjectComboBox.getValue();

        if(subject == null){
            return;
        }

        try {
            DatabaseConnection database = new DatabaseConnection();

            database.finalizeGradesForSubject(database.getSubjectIDByName(subject));

            showInfo("Finalized", "Grades finalized.");

            loadGrades();
        } catch (Exception e) {
            showError("Finalize Error", "Failed to finalize grades.");
        }
    }

    private void clearTable() {
        studentGradeTable.getItems().clear();
        studentGradeTable.getColumns().clear();
    }

    private void updateUI() {
        studentGradeTable.setEditable(!gradesFinalized);
        submitButton.setDisable(gradesFinalized);
        finalizeButton.setDisable(gradesFinalized);
    }

    private void showError(String t, String m) {
        new Alert(Alert.AlertType.ERROR, m).showAndWait();
    }

    private void showWarning(String t, String m) {
        new Alert(Alert.AlertType.WARNING, m).showAndWait();
    }

    private void showInfo(String t, String m) {
        new Alert(Alert.AlertType.INFORMATION, m).showAndWait();
    }
}