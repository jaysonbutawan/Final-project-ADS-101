package com.example.sapacoordinator.HospitalComponents;

import com.google.gson.annotations.SerializedName;

public class Hospital {

    @SerializedName("hospital_id")
    private int hospitalId;

    @SerializedName("hospital_name")
    private String hospitalName;

    @SerializedName("hospital_address")
    private String hospitalAddress;

    @SerializedName("contact_info")
    private String contactInfo;

    @SerializedName("descriptions")
    private String descriptions;

    public Hospital() {}

    public Hospital(int hospitalId, String hospitalName, String hospitalAddress, String contactInfo, String descriptions) {
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.hospitalAddress = hospitalAddress;
        this.contactInfo = contactInfo;
        this.descriptions = descriptions;
    }

    public int getHospitalId() {
        return hospitalId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public String getHospitalAddress() {
        return hospitalAddress;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public String getDescriptions() {
        return descriptions;
    }
}
