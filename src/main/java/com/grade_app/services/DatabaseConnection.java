package com.grade_app.services;

import com.grade_app.model.Course;
import com.grade_app.model.Student;
import com.grade_app.model.Subject;
import com.grade_app.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "SELECT userID, name, surname, email, role, year FROM Users WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    int userID = result.getInt("userID");
                    String name = result.getString("name");
                    String surname = result.getString("surname");
                    String emailDB = result.getString("email");
                    String role = result.getString("role");

                    if(role.equals("student")){
                        int year = result.getInt("year");

                        return new Student(userID, name, surname, emailDB, role, year);
                    }else{
                        return new User(userID, name, surname, emailDB, role);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Course> getCourses(){
        List<Course> courses = new ArrayList<>();

        String sql = "SELECT courseID, courseName FROM Courses";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet result = statement.executeQuery();

            while(result.next()){
                courses.add(new Course(result.getInt("courseID"), result.getString("courseName")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }

    public List<Subject> getSubjects(int courseID){
        List<Subject> subjects = new ArrayList<>();

        String sql = "SELECT Subjects.subjectID, Subjects.subjectName, Subjects.credits FROM Subjects, Course_Subjects WHERE Subjects.subjectID = Course_Subjects.subjectID AND Course_Subjects.courseID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, courseID);

            try (ResultSet result = statement.executeQuery()) {
                while(result.next()){
                    subjects.add(new Subject(result.getInt("subjectID"), result.getString("subjectName"), result.getInt("credits")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subjects;
    }

    public boolean adminRegister(String name, String surname, String email, String password, String role, int year, Course course, Subject subject){
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        switch (role) {
            case "admin" -> {
                String sql = "INSERT INTO Users(name, surname, email, password, role) Values(?, ?, ?, ?, ?)";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, name);
                    statement.setString(2, surname);
                    statement.setString(3, email);
                    statement.setString(4, hashedPassword);
                    statement.setString(5, role);

                    int success = statement.executeUpdate();

                    return success > 0;
                } catch (SQLException e) {
                    e.printStackTrace();

                    return false;
                }
            }
            case "teacher" -> {
                String sql1 = "INSERT INTO Users(name, surname, email, password, role) Values(?, ?, ?, ?, ?)";

                try (PreparedStatement statement1 = connection.prepareStatement(sql1, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    statement1.setString(1, name);
                    statement1.setString(2, surname);
                    statement1.setString(3, email);
                    statement1.setString(4, hashedPassword);
                    statement1.setString(5, role);

                    int successful = statement1.executeUpdate();

                    if(successful > 0){
                        ResultSet generatedKeys = statement1.getGeneratedKeys();

                        if(generatedKeys.next()) {
                            int teacherID = generatedKeys.getInt(1);

                            String sql2 = "INSERT INTO Teacher_Subject(teacherID, subjectID, courseID) VALUES(?, ?, ?)";

                            try (PreparedStatement statement2 = connection.prepareStatement(sql2)) {
                                statement2.setInt(1, teacherID);
                                statement2.setInt(2, subject.getId());
                                statement2.setInt(3, course.getId());

                                statement2.executeUpdate();
                            }
                        }
                    }

                    return successful > 0;
                } catch (SQLException e) {
                    e.printStackTrace();

                    return false;
                }
            }
            case "student" -> {
                String sql1 = "INSERT INTO Users(name, surname, email, password, role, year) VALUES(?, ?, ?, ?, ?, ?)";

                try (PreparedStatement statement1 = connection.prepareStatement(sql1, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    statement1.setString(1, name);
                    statement1.setString(2, surname);
                    statement1.setString(3, email);
                    statement1.setString(4, hashedPassword);
                    statement1.setString(5, role);
                    statement1.setInt(6,year);

                    int successful = statement1.executeUpdate();

                    if(successful > 0){
                        ResultSet generatedKeys = statement1.getGeneratedKeys();

                        if(generatedKeys.next()){
                            int studentID = generatedKeys.getInt(1);

                            String sql2 = "INSERT INTO Course_Student(studentID, courseID) VALUES (?, ?)";

                            try (PreparedStatement statement2 = connection.prepareStatement(sql2)) {
                                statement2.setInt(1, studentID);
                                statement2.setInt(2, course.getId());

                                statement2.executeUpdate();
                            }

                            String sql3 = "INSERT INTO Grades (studentID, subjectID, subjectAssessmentID, gradeValue) SELECT ?, cs.subjectID, sa.subjectAssessmentID, 0 FROM Course_Subjects cs, Subject_Assessments sa WHERE cs.courseID = ? AND cs.subjectYear = ? AND sa.subjectID = cs.subjectID";

                            try (PreparedStatement statement3 = connection.prepareStatement(sql3)) {
                                statement3.setInt(1, studentID);
                                statement3.setInt(2, course.getId());
                                statement3.setInt(3, year);

                                statement3.executeUpdate();
                            }
                        }
                    }

                    return successful > 0;
                } catch (SQLException e) {
                    e.printStackTrace();

                    return false;
                }
            }
        }

        return false;
    }

    public List<User> getAllUsers(){
        List<User> users = new ArrayList<>();

        String sql = "SELECT userID, name, surname, email, role FROM Users";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet result = statement.executeQuery();

            while(result.next()){
                users.add(new User(result.getInt("userID"), result.getString("name"), result.getString("surname"), result.getString("email"), result.getString("role")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public User searchUser(String name, String surname){
        String sql = "SELECT userID, name, surname, email, role FROM Users WHERE name = ? AND surname = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, surname);

            try (ResultSet result = statement.executeQuery()){
                if(result.next()){
                    return new User(result.getInt("userID"), result.getString("name"), result.getString("surname"), result.getString("email"), result.getString("role"));
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean deleteUser(String name, String surname){
        String sql = "DELETE FROM Users WHERE name = ? AND surname = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, surname);

            int success = statement.executeUpdate();

            return success > 0;
        } catch(SQLException e) {
            e.printStackTrace();

            return false;
        }
    }

    public String getCoursePerStudent(int userId){
        String sql = "SELECT Courses.courseName FROM Courses, Course_Student, Users WHERE Course_Student.studentID = ? AND Courses.courseID = Course_Student.courseID";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                if(result.next()){
                    return result.getString("courseName");
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return " ";
    }
}