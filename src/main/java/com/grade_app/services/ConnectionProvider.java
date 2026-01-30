package com.grade_app.services;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionProvider {
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if(connection == null){
            Dotenv dotenv = Dotenv.load();

            String url = dotenv.get("DB_URL");
            String user = dotenv.get("DB_USER");
            String pass = dotenv.get("DB_PASS");

            connection = DriverManager.getConnection(url, user, pass);
        }

        return connection;
    }
}