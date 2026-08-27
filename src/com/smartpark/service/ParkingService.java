package com.smartpark.service;

import com.smartpark.dao.DBConnection;
import com.smartpark.dao.ParkingRecordDAO;
import com.smartpark.dao.ParkingSlotDAO;
import com.smartpark.model.BillingReceipt;
import com.smartpark.model.DashboardStats;
import com.smartpark.model.ParkingRecord;
import com.smartpark.model.ParkingSlot;
import com.smartpark.util.DBConfig;
import com.smartpark.util.DateTimeUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ParkingService manages the end-to-end vehicle entry, slot allocation, exit processing,
 * and real-time dashboard calculations.
 */
public class ParkingService {
    private final ParkingSlotDAO slotDAO = new ParkingSlotDAO();
    private final ParkingRecordDAO recordDAO = new ParkingRecordDAO();
    private final BillingService billingService = new BillingService();

    /**
     * Processes vehicle check-in:
     * 1. Validates vehicle number.
     * 2. Checks if vehicle is already parked.
     * 3. Finds compatible available slot.
     * 4. Stores entry transaction and changes slot to OCCUPIED.
     */
    public Map<String, Object> processEntry(String vehicleNumber, String vehicleType) throws Exception {
        if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle number is required.");
        }
        vehicleNumber = vehicleNumber.trim().toUpperCase().replaceAll("\\s+", " ");
        if (vehicleNumber.length() < 3 || vehicleNumber.length() > 20) {
            throw new IllegalArgumentException("Invalid vehicle number format.");
        }

        if (vehicleType == null || (!vehicleType.equalsIgnoreCase("2W") && !vehicleType.equalsIgnoreCase("4W"))) {
            throw new IllegalArgumentException("Vehicle category must be either '2W' or '4W'.");
        }
        vehicleType = vehicleType.toUpperCase();

        // Check if vehicle is already actively parked
        ParkingRecord existing = recordDAO.getActiveRecordByVehicleNumber(vehicleNumber);
        if (existing != null) {
            throw new IllegalArgumentException("Vehicle " + vehicleNumber + " is already parked in Slot " + existing.getSlotNumber() + ".");
        }

        // Check if vehicle has a pre-booking reservation or monthly pass reserved slot
        com.smartpark.dao.ReservationDAO reservationDAO = new com.smartpark.dao.ReservationDAO();
        com.smartpark.dao.MonthlyPassDAO monthlyPassDAO = new com.smartpark.dao.MonthlyPassDAO();

        com.smartpark.model.Reservation reservation = reservationDAO.getByPassCodeOrVehicle(vehicleNumber);
        com.smartpark.model.MonthlyPass monthlyPass = monthlyPassDAO.getActivePassByVehicle(vehicleNumber);

        ParkingSlot slot = null;
        if (reservation != null) {
            slot = slotDAO.getSlotById(reservation.getSlotId());
        } else if (monthlyPass != null) {
            slot = slotDAO.getSlotById(monthlyPass.getSlotId());
        }

        // Fallback to automatic slot allocation if no pre-booking or pass exists
        if (slot == null || (!"RESERVED".equals(slot.getStatus()) && !"MONTHLY_PASS".equals(slot.getStatus()) && !"AVAILABLE".equals(slot.getStatus()))) {
            slot = slotDAO.getAvailableSlot(vehicleType);
        }

        if (slot == null) {
            throw new IllegalStateException("No available parking slot for " + (vehicleType.equals("4W") ? "Four Wheeler (4W)" : "Two Wheeler (2W)") + ".");
        }

        // If pre-booking existed, mark reservation status as CHECKED_IN
        if (reservation != null) {
            reservationDAO.updateStatus(reservation.getReservationId(), "CHECKED_IN");
        }

        LocalDateTime entryTime = LocalDateTime.now();

        // Transactional insertion and slot status update
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            ParkingRecord record = new ParkingRecord();
            record.setVehicleNumber(vehicleNumber);
            record.setVehicleType(vehicleType);
            record.setSlotId(slot.getSlotId());
            record.setEntryTime(entryTime);
            record.setStatus("PARKED");

            int recordId = recordDAO.insertEntryRecord(conn, record);
            slotDAO.updateSlotStatus(conn, slot.getSlotId(), "OCCUPIED");

            conn.commit();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("recordId", recordId);
            result.put("vehicleNumber", vehicleNumber);
            result.put("vehicleType", vehicleType);
            result.put("slotId", slot.getSlotId());
            result.put("slotNumber", slot.getSlotNumber());
            result.put("floorLevel", slot.getFloorLevel());
            result.put("entryTime", DateTimeUtil.formatForDisplay(entryTime));
            result.put("entryTimeRaw", DateTimeUtil.formatForDb(entryTime));
            result.put("message", "Vehicle parked successfully! Assigned Slot: " + slot.getSlotNumber());
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException se) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException se) {}
            }
        }
    }

    /**
     * Processes vehicle check-out:
     * 1. Finds active parking record.
     * 2. Calculates duration and billing amount based on rates and dynamic pricing.
     * 3. Updates record to COMPLETED and frees slot to AVAILABLE.
     */
    public BillingReceipt processExit(String vehicleNumber) throws Exception {
        if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle number is required.");
        }
        vehicleNumber = vehicleNumber.trim().toUpperCase();

        ParkingRecord record = recordDAO.getActiveRecordByVehicleNumber(vehicleNumber);
        if (record == null) {
            throw new IllegalArgumentException("Vehicle not found in active parking: " + vehicleNumber);
        }

        LocalDateTime exitTime = LocalDateTime.now();

        // Calculate occupancy % for dynamic pricing check
        int total = slotDAO.getTotalCount();
        int occupied = slotDAO.getOccupiedCount();
        double occupancyRate = total > 0 ? ((double) occupied / total) * 100.0 : 0.0;

        BillingReceipt receipt = billingService.calculateBill(
            record.getRecordId(),
            record.getVehicleNumber(),
            record.getVehicleType(),
            record.getSlotNumber(),
            record.getEntryTime(),
            exitTime,
            occupancyRate
        );

        // Transactional update
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            record.setExitTime(exitTime);
            record.setDurationMinutes(receipt.getDurationMinutes());
            record.setBillableHours(receipt.getBillableHours());
            record.setHourlyRate(receipt.getHourlyRate());
            record.setDynamicPricingApplied(receipt.isDynamicPricingApplied());
            record.setTotalAmount(receipt.getTotalAmount());
            record.setStatus("COMPLETED");

            recordDAO.updateExitRecord(conn, record);
            slotDAO.updateSlotStatus(conn, record.getSlotId(), "AVAILABLE");

            conn.commit();
            return receipt;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException se) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException se) {}
            }
        }
    }

    /**
     * Aggregates real-time statistics directly from database.
     */
    public DashboardStats getDashboardStats() throws SQLException {
        int total = slotDAO.getTotalCount();
        int occupied = slotDAO.getOccupiedCount();
        int available = slotDAO.getAvailableCount();
        int reserved = slotDAO.getReservedCount();

        int total2W = slotDAO.getTotalCountByType("2W");
        int occupied2W = slotDAO.getCountByTypeAndStatus("2W", "OCCUPIED");
        int available2W = slotDAO.getCountByTypeAndStatus("2W", "AVAILABLE");

        int total4W = slotDAO.getTotalCountByType("4W");
        int occupied4W = slotDAO.getCountByTypeAndStatus("4W", "OCCUPIED");
        int available4W = slotDAO.getCountByTypeAndStatus("4W", "AVAILABLE");

        double occupancyPercentage = total > 0 ? ((double) occupied / total) * 100.0 : 0.0;
        boolean dynamicPricingActive = occupancyPercentage > DBConfig.getOccupancyThreshold();

        double revenue = recordDAO.getTodayRevenue();

        DashboardStats stats = new DashboardStats();
        stats.setTotalSlots(total);
        stats.setOccupiedSlots(occupied);
        stats.setAvailableSlots(available);
        stats.setReservedSlots(reserved);
        stats.setOccupancyPercentage(occupancyPercentage);
        stats.setDynamicPricingActive(dynamicPricingActive);
        stats.setTotal2W(total2W);
        stats.setOccupied2W(occupied2W);
        stats.setAvailable2W(available2W);
        stats.setTotal4W(total4W);
        stats.setOccupied4W(occupied4W);
        stats.setAvailable4W(available4W);
        stats.setTotalRevenueToday(revenue);

        return stats;
    }

    public List<ParkingSlot> getAllSlots() throws SQLException {
        return slotDAO.getAllSlots();
    }

    public List<ParkingRecord> getRecentHistory(int limit) throws SQLException {
        return recordDAO.getRecentHistory(limit);
    }

    public List<ParkingRecord> getActiveRecords() throws SQLException {
        return recordDAO.getAllActiveRecords();
    }
}
