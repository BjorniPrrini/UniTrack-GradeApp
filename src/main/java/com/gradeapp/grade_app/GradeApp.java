package com.gradeapp.grade_app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GradeApp extends Application {
    private static Scene scene;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        scene = new Scene(loadFxml("Role"));
        stage.setScene(scene);
        stage.setTitle("GradeApp");
        stage.setMaximized(true);
        stage.show();
    }

    private static Parent loadFxml(String role) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GradeApp.class.getResource("/com/gradeapp/grade_app/ui/" + role + ".fxml"));

        return fxmlLoader.load();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFxml(fxml));
    }
}