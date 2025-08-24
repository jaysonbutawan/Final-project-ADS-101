package com.example.sapacoordinator.SchoolComponents.StudentsComponents;

import com.google.gson.annotations.SerializedName;

public class Student {

        @SerializedName("student_id")
        private int studentId;

        @SerializedName("student_code")
        private String studentCode;

        @SerializedName("firstname")
        private String firstname;

        @SerializedName("lastname")
        private String lastname;

        @SerializedName("phone_number")
        private String phoneNumber;

        @SerializedName("email")
        private String email;

        @SerializedName("sex")
        private String sex;

        @SerializedName("age")
        private int age;

        @SerializedName("school_id")
        private int schoolId;



    public Student() {
        // Empty constructor for Firebase, Retrofit, etc.
    }

    public Student(int studentId, String studentCode, String firstname, String lastname, String phoneNumber, String email, String sex, int age, int schoolId) {
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.sex = sex;
        this.age = age;
        this.schoolId = schoolId;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(int schoolId) {
        this.schoolId = schoolId;
    }
}

