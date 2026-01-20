package com.grade_app.services;

import com.grade_app.model.*;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

                            String sql3 =  "INSERT INTO Grades (studentID, subjectID, subjectAssessmentID, gradeValue) SELECT ?, Subject_Assessments.subjectID, Subject_Assessments.subjectAssessmentID, 0 FROM Course_Subjects, Subject_Assessments WHERE Course_Subjects.courseID = ? AND Course_Subjects.subjectYear = ? AND Subject_Assessments.subjectID = Course_Subjects.subjectID AND Subject_Assessments.active = 1";

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

        return "";
    }

    public List<String> getSubjectNotInTeacher(int teacherID) {
        List<String> subjects = new ArrayList<>();

        String sql = "SELECT subjectName FROM Subjects WHERE subjectID NOT IN (SELECT subjectID FROM Teacher_Subject WHERE teacherID = ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, teacherID);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    subjects.add(result.getString("subjectName"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subjects;
    }

    public List<String> getSubjectInTeacher(int teacherID) {
        List<String> subjects = new ArrayList<>();

        String sql = "SELECT subjectName FROM Subjects WHERE subjectID IN (SELECT subjectID FROM Teacher_Subject WHERE teacherID = ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, teacherID);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    subjects.add(result.getString("subjectName"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subjects;
    }

    public boolean submitAssignedSubject(int teacherID, String chosenSubject, String choice) {
        String sql1 = "SELECT subjectID, (SELECT courseID FROM Course_Subjects WHERE subjectID = Subjects.subjectID) AS courseID FROM Subjects WHERE subjectName = ?";
        int subjectID = -1;
        int courseID = -1;

        try (PreparedStatement statement1 = connection.prepareStatement(sql1)) {
            statement1.setString(1, chosenSubject);

            try (ResultSet result1 = statement1.executeQuery()) {
                if (result1.next()) {
                    subjectID = result1.getInt("subjectID");
                    courseID = result1.getInt("courseID");
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }

        String sql2 = "";

        if(choice.equalsIgnoreCase("Add")){
            sql2 = "INSERT INTO Teacher_Subject(teacherID, subjectID, courseID) VALUES(?, ?, ?)";
        }else if(choice.equalsIgnoreCase("Delete")){
            sql2 = "DELETE FROM Teacher_Subject WHERE teacherID = ? AND subjectID = ? AND courseID = ?";
        }

        try (PreparedStatement statement2 = connection.prepareStatement(sql2)) {
            statement2.setInt(1, teacherID);
            statement2.setInt(2, subjectID);
            statement2.setInt(3, courseID);

            int successful = statement2.executeUpdate();

            return successful > 0;
        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }
    }


    public Map<String, Integer> getSubjectAssessments(int subjectID) {
        Map<String, Integer> assessments = new HashMap<>();

        String sql = "SELECT Assessments.assessmentName, Subject_Assessments.weight FROM Assessments, Subject_Assessments WHERE Subject_Assessments.subjectID = ? AND Subject_Assessments.assessmentID = Assessments.assessmentID AND Subject_Assessments.active = 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, subjectID);

            try (ResultSet result = statement.executeQuery()) {
                while(result.next()){
                    assessments.put(result.getString("assessmentName"), result.getInt("weight"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return assessments;
    }

    public boolean setAssessmentActive(int subjectID, String assessmentName, boolean active) {
        String sql = "UPDATE Subject_Assessments SET active = ? WHERE subjectID = ? AND assessmentID = (SELECT assessmentID FROM Assessments WHERE assessmentName = ?)";

        int activeInt  = active ? 1 : 0;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, activeInt);
            statement.setInt(2, subjectID);
            statement.setString(3, assessmentName);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    public boolean addOrUpdateSubjectAssessment(int subjectID, String assessmentName, int weight, boolean active) {
        int activeInt  = active ? 1 : 0;

        int assessmentID = -1;

        String sql1 = "SELECT assessmentID FROM Assessments WHERE assessmentName = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql1)) {
            statement.setString(1, assessmentName);

            try (ResultSet result = statement.executeQuery()) {
                if(result.next()){
                    assessmentID = result.getInt("assessmentID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }

        if (assessmentID == -1) {
            String sql2 = "INSERT INTO Assessments (assessmentName) VALUES (?)";

            try (PreparedStatement statement = connection.prepareStatement(sql2, PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, assessmentName);
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        assessmentID = keys.getInt(1);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();

                return false;
            }
        }

        int subjectAssessmentID = -1;

        String sql3 = "SELECT subjectAssessmentID FROM Subject_Assessments WHERE subjectID = ? AND assessmentID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql3)) {
            statement.setInt(1, subjectID);
            statement.setInt(2, assessmentID);

            try (ResultSet result = statement.executeQuery()) {
                if(result.next()){
                    subjectAssessmentID = result.getInt("subjectAssessmentID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }

        if(subjectAssessmentID != -1){
            String sql4 = "UPDATE Subject_Assessments SET weight = ?, active = ? WHERE subjectAssessmentID = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql4)) {
                statement.setInt(1, weight);
                statement.setInt(2, activeInt);
                statement.setInt(3, subjectAssessmentID);

                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();

                return false;
            }
        }else{
            String sql5 = "INSERT INTO Subject_Assessments (subjectID, assessmentID, weight, active) VALUES (?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql5)) {
                statement.setInt(1, subjectID);
                statement.setInt(2, assessmentID);
                statement.setInt(3, weight);
                statement.setInt(4, activeInt);

                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();

                return false;
            }
        }

        return true;
    }

    public List<Subject> getSubjectsForStudent(int studentID) throws SQLException{
        String sql = "SELECT Subjects.subjectID, Subjects.subjectName, Subjects.credits FROM Subjects, Course_Subjects, Course_Student WHERE Subjects.subjectID = Course_Subjects.subjectID AND Course_Subjects.courseID = Course_Student.courseID AND Course_Student.studentID = ? GROUP BY Subjects.subjectID, Subjects.subjectName, Subjects.credits";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, studentID);

        ResultSet result = statement.executeQuery();

        List<Subject> subjects = new ArrayList<>();

        while(result.next()){
            subjects.add(new Subject(result.getInt("subjectID"), result.getString("subjectName"), result.getInt("credits")));
        }

        return subjects;
    }

    public List<Assessment> getAssessmentsForStudent(int studentID, int subjectID) throws SQLException{
        String sql = "SELECT Subject_Assessments.subjectAssessmentID, Assessments.assessmentName, Subject_Assessments.weight, COALESCE((SELECT Grades.gradeValue FROM Grades WHERE Grades.subjectAssessmentID = Subject_Assessments.subjectAssessmentID AND Grades.studentID = ?), 0) AS gradeValue FROM Subject_Assessments, Assessments WHERE Subject_Assessments.assessmentID = Assessments.assessmentID AND Subject_Assessments.subjectID = ? AND Subject_Assessments.active = 1 ORDER BY Subject_Assessments.subjectAssessmentID";

        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, studentID);
        statement.setInt(2, subjectID);

        ResultSet result = statement.executeQuery();

        List<Assessment> assessments = new ArrayList<>();

        while(result.next()){
            assessments.add(new Assessment(result.getInt("subjectAssessmentID"), result.getString("assessmentName"), result.getInt("weight"), result.getInt("gradeValue")));
        }

        return assessments;
    }
}