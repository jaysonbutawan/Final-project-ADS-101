package com.example.sapacoordinator.SchoolComponents.StudentsComponents;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Student  implements Parcelable {

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

    // ✅ Constructor for Parcelable
    protected Student(Parcel in) {
        studentId = in.readInt();
        studentCode = in.readString();
        firstname = in.readString();
        lastname = in.readString();
        phoneNumber = in.readString();
        email = in.readString();
        sex = in.readString();
        age = in.readInt();
        schoolId = in.readInt();
    }

    // ✅ Parcelable CREATOR
    public static final Parcelable.Creator<Student> CREATOR = new Parcelable.Creator<Student>() {
        @Override
        public Student createFromParcel(Parcel in) {
            return new Student(in);
        }

        @Override
        public Student[] newArray(int size) {
            return new Student[size];
        }
    };


    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(studentId);
        dest.writeString(studentCode);
        dest.writeString(firstname);
        dest.writeString(lastname);
        dest.writeString(phoneNumber);
        dest.writeString(email);
        dest.writeString(sex);
        dest.writeInt(age);
        dest.writeInt(schoolId);
    }


    public int describeContents() {
        return 0;
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

