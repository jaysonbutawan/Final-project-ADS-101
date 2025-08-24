package com.example.sapacoordinator.ViewBookingComponents.ViewBooking;

import com.google.gson.annotations.SerializedName;

public class ViewBookingModel {
    @SerializedName("student_id")
    private int studentId;
    @SerializedName("firstname")
    private String firstName;
    @SerializedName("lastname")
    private String lastName;
    @SerializedName("student_code")
    private String studentCode;
    @SerializedName("section_name")
    private String department;
    @SerializedName("slot_date")
    private String slotDate;
    @SerializedName("start_time")
    private String startTime;
    @SerializedName("end_time")
    private String endTime;
    @SerializedName("appointment_status")
    private String appointmentStatus;
    private int schoolId;

    public ViewBookingModel(int studentId, String firstName, String lastName, String studentCode, String department, String slotDate, String startTime, String endTime, String appointmentStatus, int schoolId) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentCode = studentCode;
        this.department = department;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.appointmentStatus = appointmentStatus;
        this.schoolId = schoolId;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return firstName+" "+lastName;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public String getDepartment() {
        return department;
    }

    public String getSlotDate() {
        return slotDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public int getSchoolId() {
        return schoolId;
    }
    public String getTimeSlot() {
        return getStartTime() + " - " + getEndTime();
    }


}
