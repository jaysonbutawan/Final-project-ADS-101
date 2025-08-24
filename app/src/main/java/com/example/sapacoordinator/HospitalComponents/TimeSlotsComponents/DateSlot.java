package com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents;

import com.google.gson.annotations.SerializedName;

public class DateSlot {
    @SerializedName("slot_date_id")
    private int slotDateId;
    @SerializedName("slot_date")
    private String slotDate;
    @SerializedName("department_id")
    private int departmentId;

    public DateSlot(int slotDateId, String slotDate, int departmentId) {
        this.slotDateId = slotDateId;
        this.slotDate = slotDate;
        this.departmentId = departmentId;
    }

    public int getSlotDateId() {
        return slotDateId;
    }

    public String getSlotDate() {
        return slotDate;
    }

    public int getDepartmentId() {
        return departmentId;
    }
}