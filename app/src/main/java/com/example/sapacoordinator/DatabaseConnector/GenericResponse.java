package com.example.sapacoordinator.DatabaseConnector;

import com.google.gson.annotations.SerializedName;

public class GenericResponse {
    private String status;
    private String message;
    @SerializedName("student_count")
    private int studentCount;
    @SerializedName("booking_count")
    private int booking_count;

    public int getBooking_count() {
        return booking_count;
    }
    public void setBooking_count(int booking_count) {
        this.booking_count = booking_count;
    }
    private int user_id;

    public boolean isSuccess() {
        return status != null &&
                (status.equalsIgnoreCase("success") ||
                        status.equalsIgnoreCase("ok") ||
                        status.equalsIgnoreCase("true"));
    }

    public String getMessage() {
        return message;
    }
    public int getStudent_count() { return studentCount; }
    public int getUserId() {
        return user_id;
    }
}
