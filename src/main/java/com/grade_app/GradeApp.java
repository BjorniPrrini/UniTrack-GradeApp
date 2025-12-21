package com.grade_app;

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
        scene = new Scene(loadFxml("LogInPage"));

        stage.setScene(scene);
        stage.setTitle("UniTrack");
        stage.setMaximized(false);
        stage.show();
    }

    private static Parent loadFxml(String scene) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GradeApp.class.getResource("/com/ui/" + scene + ".fxml"));

        return fxmlLoader.load();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFxml(fxml));
        Stage stage = (Stage) scene.getWindow();
        stage.sizeToScene();
    }
}