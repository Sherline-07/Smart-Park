package com.smartpark.model;

import java.sql.Date;
import java.sql.Timestamp;

public class MonthlyPass {
    private int passId;
    private int userId;
    private String vehicleNumber;
    private String vehicleType;
    private int slotId;
    private String slotNumber;
    private Date startDate;
    private Date endDate;
    private int monthsPaid;
    private double amountPaid;
    private String passCode;
    private String status; // ACTIVE, EXPIRED, CANCELLED
    private Timestamp createdAt;

    public MonthlyPass() {}

    public MonthlyPass(int passId, int userId, String vehicleNumber, String vehicleType, int slotId, String slotNumber, Date startDate, Date endDate, int monthsPaid, double amountPaid, String passCode, String status) {
        this.passId = passId;
        this.userId = userId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthsPaid = monthsPaid;
        this.amountPaid = amountPaid;
        this.passCode = passCode;
        this.status = status;
    }

    public int getPassId() { return passId; }
    public void setPassId(int passId) { this.passId = passId; }

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

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public int getMonthsPaid() { return monthsPaid; }
    public void setMonthsPaid(int monthsPaid) { this.monthsPaid = monthsPaid; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }

    public String getPassCode() { return passCode; }
    public void setPassCode(String passCode) { this.passCode = passCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
