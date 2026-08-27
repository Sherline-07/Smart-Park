package com.smartpark.model;

import java.sql.Timestamp;

public class SavedVehicle {
    private int vehicleId;
    private int userId;
    private String vehicleNumber;
    private String vehicleType;
    private Timestamp createdAt;

    public SavedVehicle() {}

    public SavedVehicle(int vehicleId, int userId, String vehicleNumber, String vehicleType) {
        this.vehicleId = vehicleId;
        this.userId = userId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
    }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
