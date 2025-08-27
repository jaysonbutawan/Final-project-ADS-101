package com.example.sapacoordinator.DatabaseConnector;

public class TimeSlotCapacityResponse {
    private boolean success;
    private String message;
    private int maxCapacity;
    private int currentBookings;
    private int availableSlots;
    private boolean isFull;
    public TimeSlotCapacityResponse() {
    }

    public TimeSlotCapacityResponse(boolean success, String message, int maxCapacity, int currentBookings, int availableSlots, boolean isFull) {
        this.success = success;
        this.message = message;
        this.maxCapacity = maxCapacity;
        this.currentBookings = currentBookings;
        this.availableSlots = availableSlots;
        this.isFull = isFull;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getCurrentBookings() {
        return currentBookings;
    }

    public void setCurrentBookings(int currentBookings) {
        this.currentBookings = currentBookings;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }

    public boolean isFull() {
        return isFull;
    }

    public void setFull(boolean full) {
        isFull = full;
    }
}
