package com.gradeapp.grade_app.services;

import java.sql.Connection;

public class DatabaseConnection {
    private Connection connection;

    public DatabaseConnection(Connection connection) {
        this.connection = connection;
    }
}