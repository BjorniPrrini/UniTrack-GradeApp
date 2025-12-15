module com.gradeapp.grade_app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.gradeapp.grade_app to javafx.fxml;
    exports com.gradeapp.grade_app;
}