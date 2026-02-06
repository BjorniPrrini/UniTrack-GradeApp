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

    public List<Subject> getSubjectsForStudent(int studentID, int studentYear) throws SQLException{
        String sql = "SELECT Subjects.subjectID, Subjects.subjectName, Subjects.credits FROM Subjects, Course_Subjects, Course_Student WHERE Subjects.subjectID = Course_Subjects.subjectID AND Course_Subjects.courseID = Course_Student.courseID AND Course_Student.studentID = ? AND Course_Subjects.subjectYear <= ? GROUP BY Subjects.subjectID, Subjects.subjectName, Subjects.credits ORDER BY Course_Subjects.subjectYear, Subjects.subjectName";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, studentID);
        statement.setInt(2, studentYear);

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

    public int getSubjectIDByName(String subjectName){
        String sql = "SELECT subjectID FROM Subjects WHERE subjectName = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, subjectName);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("subjectID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public List<StudentGrade> getStudentGradesForSubject(int subjectID){
        List<StudentGrade> studentGrades = new ArrayList<>();

        String sql = "SELECT DISTINCT Users.userID AS studentID, CONCAT(Users.name, ' ', Users.surname) AS studentName, Grades.gradeID, Grades.subjectAssessmentID, Assessments.assessmentName, Grades.gradeValue, Grades.finalized FROM Users INNER JOIN Grades ON Users.userID = Grades.studentID INNER JOIN Subject_Assessments ON Grades.subjectAssessmentID = Subject_Assessments.subjectAssessmentID INNER JOIN Assessments ON Subject_Assessments.assessmentID = Assessments.assessmentID WHERE Grades.subjectID = ? AND Subject_Assessments.active = 1 ORDER BY Users.surname, Users.name, Subject_Assessments.subjectAssessmentID";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, subjectID);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    studentGrades.add(new StudentGrade(result.getInt("studentID"), result.getString("studentName"), result.getInt("gradeID"), result.getInt("subjectAssessmentID"), result.getString("assessmentName"), result.getInt("gradeValue"), result.getBoolean("finalized")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return studentGrades;
    }

    public boolean updateGrade(int gradeID, int gradeValue){
        String sql = "UPDATE Grades SET gradeValue = ? WHERE gradeID = ? AND finalized = 0";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, gradeValue);
            statement.setInt(2, gradeID);

            int success = statement.executeUpdate();

            return success > 0;
        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }
    }

    public boolean finalizeGradesForSubject(int subjectID){
        String sql = "UPDATE Grades SET finalized = 1 WHERE subjectID = ? AND finalized = 0";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, subjectID);

            int success = statement.executeUpdate();

            return success > 0;
        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }
    }

    public boolean areGradesFinalized(int subjectID){
        String sql = "SELECT COUNT(*) as unfinalizedCount FROM Grades WHERE subjectID = ? AND finalized = 0";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, subjectID);

            try (ResultSet result = statement.executeQuery()) {
                if(result.next()){
                    return result.getInt("unfinalizedCount") == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Map<String, Integer> getAssessmentWeightsForSubject(int subjectID) throws SQLException {

        String sql = "SELECT a.assessmentName, sa.weight FROM Subject_Assessments sa JOIN Assessments a ON sa.assessmentID = a.assessmentID WHERE sa.subjectID = ? AND sa.active = 1";

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, subjectID);

        ResultSet result = statement.executeQuery();

        Map<String, Integer> weights = new HashMap<>();

        while(result.next()){
            weights.put(result.getString("assessmentName"), result.getInt("weight"));
        }

        return weights;
    }

    public boolean changePassword(String email, String oldPassword, String newPassword){
        if(isValidUser(email, oldPassword)){
            String sql = "UPDATE Users SET password = ? WHERE email = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

                statement.setString(1, hashedNewPassword);
                statement.setString(2, email);

                statement.executeUpdate();

                int rowsAffected = statement.executeUpdate();

                return rowsAffected == 1;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
    // 1. Get all active assessments for a subject (without JOIN)
    public Map<Integer, Integer> getSubjectAssessmentWeights(int subjectID) {
        Map<Integer, Integer> weights = new HashMap<>();
        String sql = "SELECT subjectAssessmentID, weight FROM Subject_Assessments WHERE subjectID = ? AND active = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, subjectID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int subAssID = rs.getInt("subjectAssessmentID");
                int weight = rs.getInt("weight");
                weights.put(subAssID, weight);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return weights;
    }

    // 2. Get assessment name from subjectAssessmentID
    public String getAssessmentName(int subjectAssessmentID) {
        String name = "";
        String sql = "SELECT assessmentName FROM Assessments WHERE assessmentID = " +
                "(SELECT assessmentID FROM Subject_Assessments WHERE subjectAssessmentID = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, subjectAssessmentID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                name = rs.getString("assessmentName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }

    // 3. Get the student's grade for a given subjectAssessmentID
    public int getStudentGrade(int studentID, int subjectAssessmentID) {
        int grade = 0;
        String sql = "SELECT gradeValue FROM Grades WHERE studentID = ? AND subjectAssessmentID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentID);
            stmt.setInt(2, subjectAssessmentID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                grade = rs.getInt("gradeValue");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grade;
    }

    // Optional helper: get courseID from courseName
    public int getCourseIDByName(String courseName){
        int id = -1;
        String sql = "SELECT courseID FROM Courses WHERE courseName = ?";
        try(PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1, courseName);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                id = rs.getInt("courseID");
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
        return id;
    }
    public int getStudentGrade(int studentID, int subjectID, String assessmentName) {
        try {
            // 1️⃣ Get assessmentID from Assessments table
            int assessmentID = -1;
            String sql1 = "SELECT assessmentID FROM Assessments WHERE assessmentName = ?";
            try (PreparedStatement stmt1 = connection.prepareStatement(sql1)) {
                stmt1.setString(1, assessmentName);
                ResultSet rs1 = stmt1.executeQuery();
                if (rs1.next()) assessmentID = rs1.getInt("assessmentID");
            }
            if (assessmentID == -1) return -1; // assessment not found

            // 2️⃣ Get subjectAssessmentID from Subject_Assessments table
            int subjectAssessmentID = -1;
            String sql2 = "SELECT subjectAssessmentID FROM Subject_Assessments WHERE subjectID = ? AND assessmentID = ? AND active = 1";
            try (PreparedStatement stmt2 = connection.prepareStatement(sql2)) {
                stmt2.setInt(1, subjectID);
                stmt2.setInt(2, assessmentID);
                ResultSet rs2 = stmt2.executeQuery();
                if (rs2.next()) subjectAssessmentID = rs2.getInt("subjectAssessmentID");
            }
            if (subjectAssessmentID == -1) return -1; // no active assessment

            // 3️⃣ Get gradeValue from Grades table
            String sql3 = "SELECT gradeValue FROM Grades WHERE studentID = ? AND subjectAssessmentID = ?";
            try (PreparedStatement stmt3 = connection.prepareStatement(sql3)) {
                stmt3.setInt(1, studentID);
                stmt3.setInt(2, subjectAssessmentID);
                ResultSet rs3 = stmt3.executeQuery();
                if (rs3.next()) return rs3.getInt("gradeValue");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // no grade yet
    }
    public List<Subject> getSubjectsForStudentUpToYear(int studentID, int year) {
        List<Subject> subjects = new ArrayList<>();
        String sql = """
        SELECT s.subjectID, s.subjectName, s.credits
        FROM Subjects s
        JOIN Course_Subjects cs ON cs.subjectID = s.subjectID
        JOIN Course_Student cst ON cst.courseID = cs.courseID
        WHERE cst.studentID = ?
          AND cs.subjectYear <= ?
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentID);
            stmt.setInt(2, year);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                subjects.add(new Subject(
                        rs.getInt("subjectID"),
                        rs.getString("subjectName"),
                        rs.getInt("credits")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subjects;
    }

}