package com.example.sapacoordinator.BillComponents;

import com.google.gson.annotations.SerializedName;

public class Bill {
    @SerializedName("bill_id")
    private int billId;

    @SerializedName("bill_reference")
    private String billReference;

    @SerializedName("appointment_id")
    private int appointmentId;

    @SerializedName("school_id")
    private int schoolId;

    @SerializedName("school_name")
    private String schoolName;

    @SerializedName("total_amount")
    private double totalAmount;

    @SerializedName("status")
    private String status; // "PAID" or "UNPAID"

    @SerializedName("date_issued")
    private String dateIssued;

    @SerializedName("paid_date")
    private String paidDate;


    // Constructor
    public Bill() {}

    public Bill(int billId, String billReference, int appointmentId, int schoolId,
                String schoolName, double totalAmount, String status, String dateIssued,
                String paidDate) {
        this.billId = billId;
        this.billReference = billReference;
        this.appointmentId = appointmentId;
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.dateIssued = dateIssued;
        this.paidDate = paidDate;
    }

    // Getters and Setters
    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public String getBillReference() {
        return billReference;
    }

    public void setBillReference(String billReference) {
        this.billReference = billReference;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(int schoolId) {
        this.schoolId = schoolId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateIssued() {
        return dateIssued;
    }

    public void setDateIssued(String dateIssued) {
        this.dateIssued = dateIssued;
    }

    public String getPaidDate() {
        return paidDate;
    }
    public void setPaidDate(String paidDate) {
        this.paidDate = paidDate;
    }
    public boolean isPaid() {
        return "PAID".equalsIgnoreCase(status);
    }

    public String getFormattedAmount() {
        return String.format("$%.2f", totalAmount);
    }
}
