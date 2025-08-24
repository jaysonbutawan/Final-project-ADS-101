package com.example.sapacoordinator.ViewBooking;

public class ViewBookingModel {
    private int studentId;
    private String studentName;
    private String studentCode;
    private String department;
    private String slotDate;
    private String startTime;
    private String endTime;
    private String appointmentStatus;
    private int schoolId;

    public ViewBookingModel(int studentId, String studentName, String studentCode, String department, String slotDate, String startTime, String endTime, String appointmentStatus, int schoolId) {
        this.studentId = studentId;
        this.studentName = studentName;
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
        return studentName;
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

    public String getAddedDate() {
        return slotDate;
    }


}
