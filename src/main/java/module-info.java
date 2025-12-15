module com.gradeapp.grade_app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.dotenv;
    requires java.sql;

    opens com.grade_app to javafx.fxml;
    opens com.grade_app.controllers to javafx.fxml;
    exports com.grade_app;
}