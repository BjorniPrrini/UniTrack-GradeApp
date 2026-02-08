package com.grade_app.controllers;

import com.grade_app.GradeApp;
import com.grade_app.model.User;
import com.grade_app.services.DatabaseConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class AdminDeleteController {
    @FXML
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    private Button search;

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> surnameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private Button delete;

    @FXML
    public void adminRegister() throws IOException {
        GradeApp.setRoot("AdminRegister");
    }

    @FXML
    public void adminDelete() throws IOException {
        GradeApp.setRoot("AdminDelete");
    }

    @FXML
    public void adminAssign() throws IOException {
        GradeApp.setRoot("AdminAssign");
    }

    @FXML
    public void adminSubject() throws IOException {
        GradeApp.setRoot("AdminSubjects");
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
        alert.setContentText("ARE YOU SURE YOU WANT TO LOG OUT?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            GradeApp.setRoot("LogInPage");
        }
    }

    @FXML
    public void initialize() throws SQLException {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        surnameColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        DatabaseConnection database = new DatabaseConnection();

        ObservableList<User> users = FXCollections.observableArrayList(database.getAllUsers());

        userTable.setItems(users);
    }

    @FXML
    public void searchUser() {
        search.setDisable(true);

        String name = nameField.getText();
        String surname = surnameField.getText();

        try{
            DatabaseConnection database = new DatabaseConnection();

            User user = database.searchUser(name, surname);

            ObservableList<User> userList = FXCollections.observableArrayList();

            if(user != null){
                userList.add(user);
            }else{
                showAlert("User not found", "User was not found!");
            }

            userTable.setItems(userList);
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            search.setDisable(false);
        }
    }

    @FXML
    public void deleteUser() {
        delete.setDisable(true);

        String name = nameField.getText();
        String surname = surnameField.getText();

        try{
            DatabaseConnection database = new DatabaseConnection();

            boolean success = database.deleteUser(name, surname);

            if(success){
                showSuccessAlert("User deleted", "User was successfully deleted!");

                initialize();
            }else{
                showAlert("Error", "User was not deleted!");
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            delete.setDisable(false);
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