package com.grade_app.services;

import com.grade_app.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnection {
    private final Connection connection;

    public DatabaseConnection() throws SQLException{
        this.connection = ConnectionProvider.getConnection();
    }

    public boolean isValidUser(String email, String password){
        String sql = "SELECT password FROM Users WHERE email = ?";

        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, email);

            try(ResultSet result = statement.executeQuery()){
                if(result.next()){
                    String hashedPassword = result.getString("password");

                    return BCrypt.checkpw(password, hashedPassword);
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }

        return false;
    }

    public User getUser(String email) {

        String sql = "SELECT userID, name, surname, email, role FROM Users WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new User(result.getInt("userID"), result.getString("name"), result.getString("surname"), result.getString("email"), result.getString("role"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}