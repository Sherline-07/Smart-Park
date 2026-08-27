package com.smartpark.model;

import java.sql.Timestamp;

public class Reservation {
    private int reservationId;
    private int userId;
    private String vehicleNumber;
    private String vehicleType;
    private int slotId;
    private String slotNumber;
    private Timestamp scheduledEntry;
    private int durationHours;
    private double estimatedFee;
    private String passCode;
    private String status; // CONFIRMED, CHECKED_IN, CANCELLED, EXPIRED
    private Timestamp createdAt;

    public Reservation() {}

    public Reservation(int reservationId, int userId, String vehicleNumber, String vehicleType, int slotId, String slotNumber, Timestamp scheduledEntry, int durationHours, double estimatedFee, String passCode, String status) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.scheduledEntry = scheduledEntry;
        this.durationHours = durationHours;
        this.estimatedFee = estimatedFee;
        this.passCode = passCode;
        this.status = status;
    }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public Timestamp getScheduledEntry() { return scheduledEntry; }
    public void setScheduledEntry(Timestamp scheduledEntry) { this.scheduledEntry = scheduledEntry; }

    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }

    public double getEstimatedFee() { return estimatedFee; }
    public void setEstimatedFee(double estimatedFee) { this.estimatedFee = estimatedFee; }

    public String getPassCode() { return passCode; }
    public void setPassCode(String passCode) { this.passCode = passCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
