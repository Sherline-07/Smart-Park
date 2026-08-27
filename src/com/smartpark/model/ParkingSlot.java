package com.smartpark.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ParkingSlot Entity representing individual parking spots in the facility.
 */
public class ParkingSlot {
    private int slotId;
    private String slotNumber;  // e.g. "A-01", "B-05"
    private String vehicleType; // "2W" or "4W"
    private String status;      // "AVAILABLE", "OCCUPIED", "RESERVED"
    private int floorLevel;

    public ParkingSlot() {}

    public ParkingSlot(int slotId, String slotNumber, String vehicleType, String status, int floorLevel) {
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.vehicleType = vehicleType;
        this.status = status;
        this.floorLevel = floorLevel;
    }

    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getFloorLevel() { return floorLevel; }
    public void setFloorLevel(int floorLevel) { this.floorLevel = floorLevel; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("slotId", slotId);
        map.put("slotNumber", slotNumber);
        map.put("vehicleType", vehicleType);
        map.put("status", status);
        map.put("floorLevel", floorLevel);
        return map;
    }
}
