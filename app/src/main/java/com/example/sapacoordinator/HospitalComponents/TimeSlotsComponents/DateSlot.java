package com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents;

import com.google.gson.annotations.SerializedName;

public class DateSlot {
    @SerializedName("slot_date_id")
    private int slotDateId;
    @SerializedName("slot_date")
    private String slotDate;

    public int getSlotDateId() {
        return slotDateId;
    }

    public String getSlotDate() {
        return slotDate;
    }

}