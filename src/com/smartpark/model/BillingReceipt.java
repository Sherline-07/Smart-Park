package com.smartpark.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BillingReceipt contains full itemized details for parking exit and invoice calculation.
 */
public class BillingReceipt {
    private int billId;
    private String vehicleNumber;
    private String vehicleType;
    private String slotNumber;
    private String entryTime;
    private String exitTime;
    private String durationFormatted;
    private int durationMinutes;
    private int billableHours;
    private double hourlyRate;
    private boolean dynamicPricingApplied;
    private String dynamicPricingNote;
    private double totalAmount;

    public BillingReceipt() {}

    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public String getEntryTime() { return entryTime; }
    public void setEntryTime(String entryTime) { this.entryTime = entryTime; }

    public String getExitTime() { return exitTime; }
    public void setExitTime(String exitTime) { this.exitTime = exitTime; }

    public String getDurationFormatted() { return durationFormatted; }
    public void setDurationFormatted(String durationFormatted) { this.durationFormatted = durationFormatted; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getBillableHours() { return billableHours; }
    public void setBillableHours(int billableHours) { this.billableHours = billableHours; }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }

    public boolean isDynamicPricingApplied() { return dynamicPricingApplied; }
    public void setDynamicPricingApplied(boolean dynamicPricingApplied) { this.dynamicPricingApplied = dynamicPricingApplied; }

    public String getDynamicPricingNote() { return dynamicPricingNote; }
    public void setDynamicPricingNote(String dynamicPricingNote) { this.dynamicPricingNote = dynamicPricingNote; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("billId", billId);
        map.put("vehicleNumber", vehicleNumber);
        map.put("vehicleType", vehicleType);
        map.put("slotNumber", slotNumber);
        map.put("entryTime", entryTime);
        map.put("exitTime", exitTime);
        map.put("durationFormatted", durationFormatted);
        map.put("durationMinutes", durationMinutes);
        map.put("billableHours", billableHours);
        map.put("hourlyRate", hourlyRate);
        map.put("dynamicPricingApplied", dynamicPricingApplied);
        map.put("dynamicPricingNote", dynamicPricingNote != null ? dynamicPricingNote : "");
        map.put("totalAmount", totalAmount);
        return map;
    }
}
