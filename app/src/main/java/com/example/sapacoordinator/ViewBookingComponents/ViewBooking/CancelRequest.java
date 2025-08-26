package com.example.sapacoordinator.ViewBookingComponents.ViewBooking;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CancelRequest {
    @SerializedName("school_id")
    private int school_id;

    @SerializedName("hospital_id")
    private int hospital_id;

    @SerializedName("department_id")
    private int department_id;

    @SerializedName("slot_date_id")
    private int slot_date_id;

    @SerializedName("time_slot_id")
    private int time_slot_id;

    @SerializedName("student_ids")
    private String student_ids; // send as CSV string

    public CancelRequest(int schoolId, int hospitalId, int departmentId,
                         int slotDateId, int timeSlotId, String studentIds) {
        this.school_id = schoolId;
        this.hospital_id = hospitalId;
        this.department_id = departmentId;
        this.slot_date_id = slotDateId;
        this.time_slot_id = timeSlotId;
        this.student_ids = studentIds;
    }
}

