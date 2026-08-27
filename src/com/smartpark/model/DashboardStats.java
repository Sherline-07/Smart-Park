package com.smartpark.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Live Dashboard Statistics Entity.
 */
public class DashboardStats {
    private int totalSlots;
    private int availableSlots;
    private int occupiedSlots;
    private int reservedSlots;
    private int maintenanceSlots;
    private double occupancyPercentage;
    private boolean dynamicPricingActive;
    private int available2W;
    private int occupied2W;
    private int total2W;
    private int available4W;
    private int occupied4W;
    private int total4W;
    private double totalRevenueToday;
    private double surgeRevenueToday;
    private double revenue2W;
    private double revenue4W;

    public DashboardStats() {}

    public int getTotalSlots() { return totalSlots; }
    public void setTotalSlots(int totalSlots) { this.totalSlots = totalSlots; }

    public int getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(int availableSlots) { this.availableSlots = availableSlots; }

    public int getOccupiedSlots() { return occupiedSlots; }
    public void setOccupiedSlots(int occupiedSlots) { this.occupiedSlots = occupiedSlots; }

    public int getReservedSlots() { return reservedSlots; }
    public void setReservedSlots(int reservedSlots) { this.reservedSlots = reservedSlots; }

    public int getMaintenanceSlots() { return maintenanceSlots; }
    public void setMaintenanceSlots(int maintenanceSlots) { this.maintenanceSlots = maintenanceSlots; }

    public double getOccupancyPercentage() { return occupancyPercentage; }
    public void setOccupancyPercentage(double occupancyPercentage) { this.occupancyPercentage = occupancyPercentage; }

    public boolean isDynamicPricingActive() { return dynamicPricingActive; }
    public void setDynamicPricingActive(boolean dynamicPricingActive) { this.dynamicPricingActive = dynamicPricingActive; }

    public int getAvailable2W() { return available2W; }
    public void setAvailable2W(int available2W) { this.available2W = available2W; }

    public int getOccupied2W() { return occupied2W; }
    public void setOccupied2W(int occupied2W) { this.occupied2W = occupied2W; }

    public int getTotal2W() { return total2W; }
    public void setTotal2W(int total2W) { this.total2W = total2W; }

    public int getAvailable4W() { return available4W; }
    public void setAvailable4W(int available4W) { this.available4W = available4W; }

    public int getOccupied4W() { return occupied4W; }
    public void setOccupied4W(int occupied4W) { this.occupied4W = occupied4W; }

    public int getTotal4W() { return total4W; }
    public void setTotal4W(int total4W) { this.total4W = total4W; }

    public double getTotalRevenueToday() { return totalRevenueToday; }
    public void setTotalRevenueToday(double totalRevenueToday) { this.totalRevenueToday = totalRevenueToday; }

    public double getSurgeRevenueToday() { return surgeRevenueToday; }
    public void setSurgeRevenueToday(double surgeRevenueToday) { this.surgeRevenueToday = surgeRevenueToday; }

    public double getRevenue2W() { return revenue2W; }
    public void setRevenue2W(double revenue2W) { this.revenue2W = revenue2W; }

    public double getRevenue4W() { return revenue4W; }
    public void setRevenue4W(double revenue4W) { this.revenue4W = revenue4W; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalSlots", totalSlots);
        map.put("availableSlots", availableSlots);
        map.put("occupiedSlots", occupiedSlots);
        map.put("reservedSlots", reservedSlots);
        map.put("maintenanceSlots", maintenanceSlots);
        map.put("occupancyPercentage", Math.round(occupancyPercentage * 10.0) / 10.0);
        map.put("dynamicPricingActive", dynamicPricingActive);
        map.put("available2W", available2W);
        map.put("occupied2W", occupied2W);
        map.put("total2W", total2W);
        map.put("available4W", available4W);
        map.put("occupied4W", occupied4W);
        map.put("total4W", total4W);
        map.put("totalRevenueToday", totalRevenueToday);
        map.put("surgeRevenueToday", surgeRevenueToday);
        map.put("revenue2W", revenue2W);
        map.put("revenue4W", revenue4W);
        return map;
    }
}
