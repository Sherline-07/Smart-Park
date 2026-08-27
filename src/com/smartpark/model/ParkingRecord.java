package com.smartpark.model;

import com.smartpark.util.DateTimeUtil;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ParkingRecord Entity representing active or completed parking transactions.
 */
public class ParkingRecord {
    private int recordId;
    private String vehicleNumber;
    private String vehicleType;
    private int slotId;
    private String slotNumber;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Integer durationMinutes;
    private Integer billableHours;
    private Double hourlyRate;
    private boolean dynamicPricingApplied;
    private Double totalAmount;
    private String status; // "PARKED", "COMPLETED", "CANCELLED"

    public ParkingRecord() {}

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getBillableHours() { return billableHours; }
    public void setBillableHours(Integer billableHours) { this.billableHours = billableHours; }

    public Double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }

    public boolean isDynamicPricingApplied() { return dynamicPricingApplied; }
    public void setDynamicPricingApplied(boolean dynamicPricingApplied) { this.dynamicPricingApplied = dynamicPricingApplied; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("recordId", recordId);
        map.put("vehicleNumber", vehicleNumber);
        map.put("vehicleType", vehicleType);
        map.put("slotId", slotId);
        map.put("slotNumber", slotNumber != null ? slotNumber : "Slot #" + slotId);
        map.put("entryTime", DateTimeUtil.formatForDisplay(entryTime));
        map.put("entryTimeRaw", DateTimeUtil.formatForDb(entryTime));
        map.put("exitTime", DateTimeUtil.formatForDisplay(exitTime));
        map.put("exitTimeRaw", DateTimeUtil.formatForDb(exitTime));
        map.put("durationMinutes", durationMinutes != null ? durationMinutes : 0);
        map.put("billableHours", billableHours != null ? billableHours : 0);
        map.put("hourlyRate", hourlyRate != null ? hourlyRate : 0.0);
        map.put("dynamicPricingApplied", dynamicPricingApplied);
        map.put("totalAmount", totalAmount != null ? totalAmount : 0.0);
        map.put("status", status);
        return map;
    }
}
