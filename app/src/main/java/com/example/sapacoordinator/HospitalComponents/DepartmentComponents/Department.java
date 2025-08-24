package com.example.sapacoordinator.HospitalComponents.DepartmentComponents;

import android.annotation.SuppressLint;

import com.google.gson.annotations.SerializedName;

public class Department {
    @SerializedName("department_id")
    private int department_id;
    @SerializedName("section_name")
    private String section_name;

    @SerializedName("price_per_student")
    private double price_per_student;
    @SerializedName("hospital_id")
    private int hospital_id;
    @SerializedName("hospital_name")
    private String hospital_name;


    public Department(int department_id, String section_name,double price_per_student, int hospital_id, String hospital_name) {
        this.department_id = department_id;
        this.section_name = section_name;
        this.price_per_student = price_per_student;
        this.hospital_id = hospital_id;
        this.hospital_name = hospital_name;
    }

    public String getHospital_name() {
        return hospital_name;
    }

    public int getDepartment_id() {
        return department_id;
    }

    public String getSection_name() {
        return section_name;
    }


    public double getPrice_per_student() {
        return price_per_student;
    }

    public int getHospital_id() {
        return hospital_id;
    }
}
