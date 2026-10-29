CREATE DATABASE IF NOT EXISTS Grade_App
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE Grade_App;

CREATE TABLE Users (
    userID INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    surname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(60) NOT NULL,
    role ENUM('admin', 'teacher', 'student') NOT NULL,
    year TINYINT NULL,
    PRIMARY KEY (userID),
    UNIQUE KEY uq_users_email (email)
)

CREATE TABLE Courses (
    courseID INT NOT NULL AUTO_INCREMENT,
    courseName VARCHAR(100) NOT NULL,
    duration TINYINT NOT NULL,
    PRIMARY KEY (courseID),
    UNIQUE KEY uq_courses_name (courseName)
)

CREATE TABLE Subjects (
    subjectID INT NOT NULL AUTO_INCREMENT,
    subjectName VARCHAR(100) NOT NULL,
    credits TINYINT NOT NULL,
    PRIMARY KEY (subjectID),
    UNIQUE KEY uq_subjects_name (subjectName)
)

CREATE TABLE Course_Subjects (
    courseID INT NOT NULL,
    subjectID INT NOT NULL,
    subjectYear TINYINT NOT NULL,
    PRIMARY KEY (courseID, subjectID),
    CONSTRAINT fk_cs_course FOREIGN KEY (courseID) REFERENCES Courses(courseID) ON DELETE CASCADE,
    CONSTRAINT fk_cs_subject FOREIGN KEY (subjectID) REFERENCES Subjects(subjectID) ON DELETE CASCADE
)

CREATE TABLE Course_Student (
    studentID INT NOT NULL,
    courseID INT NOT NULL,
    PRIMARY KEY (studentID, courseID),
    CONSTRAINT fk_cstu_student FOREIGN KEY (studentID) REFERENCES Users(userID) ON DELETE CASCADE,
    CONSTRAINT fk_cstu_course FOREIGN KEY (courseID) REFERENCES Courses(courseID) ON DELETE CASCADE
)

CREATE TABLE Teacher_Subject (
    teacherID INT NOT NULL,
    subjectID INT NOT NULL,
    courseID INT NOT NULL,
    PRIMARY KEY (teacherID, subjectID, courseID),
    CONSTRAINT fk_ts_teacher FOREIGN KEY (teacherID) REFERENCES Users(userID) ON DELETE CASCADE,
    CONSTRAINT fk_ts_subject FOREIGN KEY (subjectID) REFERENCES Subjects(subjectID) ON DELETE CASCADE,
    CONSTRAINT fk_ts_course FOREIGN KEY (courseID) REFERENCES Courses(courseID) ON DELETE CASCADE
)

CREATE TABLE Assessments (
    assessmentID INT NOT NULL AUTO_INCREMENT,
    assessmentName VARCHAR(50) NOT NULL,
    PRIMARY KEY (assessmentID),
    UNIQUE KEY uq_assessments_name (assessmentName)
)

CREATE TABLE Subject_Assessments (
    subjectAssessmentID INT NOT NULL AUTO_INCREMENT,
    subjectID INT NOT NULL,
    assessmentID INT NOT NULL,
    weight INT NOT NULL DEFAULT 0,
    active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (subjectAssessmentID),
    UNIQUE KEY uq_sa_subject_assessment (subjectID, assessmentID),
    CONSTRAINT fk_sa_subject    FOREIGN KEY (subjectID)    REFERENCES Subjects(subjectID)       ON DELETE CASCADE,
    CONSTRAINT fk_sa_assessment FOREIGN KEY (assessmentID) REFERENCES Assessments(assessmentID) ON DELETE CASCADE
)

CREATE TABLE Grades (
    gradeID INT NOT NULL AUTO_INCREMENT,
    studentID INT NOT NULL,
    subjectID INT NOT NULL,
    subjectAssessmentID INT NOT NULL,
    gradeValue INT NOT NULL DEFAULT 0,
    finalized TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (gradeID),
    UNIQUE KEY uq_grades_student_sa (studentID, subjectAssessmentID),
    KEY idx_grades_subject (subjectID),
    CONSTRAINT fk_g_student FOREIGN KEY (studentID) REFERENCES Users(userID) ON DELETE CASCADE,
    CONSTRAINT fk_g_subject FOREIGN KEY (subjectID) REFERENCES Subjects(subjectID) ON DELETE CASCADE,
    CONSTRAINT fk_g_sa FOREIGN KEY (subjectAssessmentID) REFERENCES Subject_Assessments(subjectAssessmentID) ON DELETE CASCADE
)

INSERT INTO Users (name, surname, email, password, role)
VALUES ('Admin', 'Admin', 'admin@gmail.com', '$2a$10$mIe6RBjB/uCOrVxOiU1SwuhS74yUBhtyf2mot.8JiiL4q0TF1xc8K', 'admin');