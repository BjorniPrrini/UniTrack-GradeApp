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

    private User selectedUser;

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

        userTable.getSelectionModel().selectedItemProperty().addListener((_, _, selected) -> {
            if(selected != null){
                selectedUser = selected;

                nameField.setText(selectedUser.getName());
                surnameField.setText(selectedUser.getSurname());
            }
        });
    }

    @FXML
    public void searchUser() {
        search.setDisable(true);

        String name = nameField.getText().trim();
        String surname = surnameField.getText().trim();

        if(name.isEmpty() || surname.isEmpty()){
            showAlert("Empty Fields", "Please enter both name and surname!");

            search.setDisable(false);

            return;
        }

        try{
            DatabaseConnection database = new DatabaseConnection();

            User user = database.searchUser(name, surname);

            ObservableList<User> userList = FXCollections.observableArrayList();

            if(user != null){
                userList.add(user);

                selectedUser = user;
            }else{
                showAlert("User not found", "User was not found!");

                selectedUser = null;
            }

            userTable.setItems(userList);
        } catch(SQLException e) {
            e.printStackTrace();

            showAlert("Database Error", "An error occurred while searching for the user!");
        } finally {
            search.setDisable(false);
        }
    }

    @FXML
    public void deleteUser() {
        delete.setDisable(true);

        if(selectedUser == null){
            showAlert("No User Selected", "Please select a user from the table or search for one first!");

            delete.setDisable(false);

            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Confirmation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete user:\n" + selectedUser.getName() + " " + selectedUser.getSurname() + "\n" + selectedUser.getEmail() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if(result.isEmpty() || result.get() != ButtonType.OK){

            delete.setDisable(false);

            return;
        }

        try{
            DatabaseConnection database = new DatabaseConnection();

            boolean success = database.deleteUser(selectedUser.getName(), selectedUser.getSurname());

            if(success){
                showSuccessAlert("User deleted", "User was successfully deleted!");

                clearSelection();

                initialize();
            }else{
                showAlert("Error", "User was not deleted!");
            }
        } catch(SQLException e) {
            e.printStackTrace();

            showAlert("Database Error", "An error occurred while deleting the user!");
        } finally {
            delete.setDisable(false);
        }
    }

    @FXML
    public void clearSelection(){
        nameField.clear();
        surnameField.clear();
        selectedUser = null;
        userTable.getSelectionModel().clearSelection();

        try {
            initialize();
        } catch (SQLException e) {
            e.printStackTrace();

            showAlert("Database Error", "An error occurred while reloading users!");
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