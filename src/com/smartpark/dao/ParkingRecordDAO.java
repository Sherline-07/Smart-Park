package com.smartpark.dao;

import com.smartpark.model.ParkingRecord;
import com.smartpark.util.DateTimeUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ParkingRecordDAO handles insertion of entry tickets, checkout billing updates, and transaction history.
 */
public class ParkingRecordDAO {

    /**
     * Finds active parking record for a vehicle by vehicle number.
     */
    public ParkingRecord getActiveRecordByVehicleNumber(String vehicleNumber) throws SQLException {
        String sql = "SELECT r.record_id, r.vehicle_number, r.vehicle_type, r.slot_id, r.entry_time, " +
                     "r.exit_time, r.duration_minutes, r.billable_hours, r.hourly_rate, r.dynamic_pricing_applied, " +
                     "r.total_amount, r.status, s.slot_number " +
                     "FROM parking_records r " +
                     "JOIN parking_slots s ON r.slot_id = s.slot_id " +
                     "WHERE UPPER(REPLACE(r.vehicle_number, ' ', '')) = UPPER(REPLACE(?, ' ', '')) " +
                     "AND r.status = 'PARKED' ORDER BY r.record_id DESC LIMIT 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, vehicleNumber.trim());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToRecord(rs);
            }
            return null;
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
    }

    /**
     * Inserts a new vehicle entry record within a transaction.
     */
    public int insertEntryRecord(Connection conn, ParkingRecord record) throws SQLException {
        String sql = "INSERT INTO parking_records (vehicle_number, vehicle_type, slot_id, entry_time, status) " +
                     "VALUES (?, ?, ?, ?, 'PARKED')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, record.getVehicleNumber().toUpperCase().trim());
            pstmt.setString(2, record.getVehicleType());
            pstmt.setInt(3, record.getSlotId());
            pstmt.setTimestamp(4, Timestamp.valueOf(record.getEntryTime()));

            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    record.setRecordId(generatedId);
                    return generatedId;
                }
            }
            return 0;
        }
    }

    /**
     * Updates an existing record on vehicle checkout with billing information.
     */
    public boolean updateExitRecord(Connection conn, ParkingRecord record) throws SQLException {
        String sql = "UPDATE parking_records SET exit_time = ?, duration_minutes = ?, billable_hours = ?, " +
                     "hourly_rate = ?, dynamic_pricing_applied = ?, total_amount = ?, status = 'COMPLETED' " +
                     "WHERE record_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(record.getExitTime()));
            pstmt.setInt(2, record.getDurationMinutes());
            pstmt.setInt(3, record.getBillableHours());
            pstmt.setDouble(4, record.getHourlyRate());
            pstmt.setBoolean(5, record.isDynamicPricingApplied());
            pstmt.setDouble(6, record.getTotalAmount());
            pstmt.setInt(7, record.getRecordId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves recent completed transactions for the History view.
     */
    public List<ParkingRecord> getRecentHistory(int limit) throws SQLException {
        List<ParkingRecord> list = new ArrayList<>();
        String sql = "SELECT r.record_id, r.vehicle_number, r.vehicle_type, r.slot_id, r.entry_time, " +
                     "r.exit_time, r.duration_minutes, r.billable_hours, r.hourly_rate, r.dynamic_pricing_applied, " +
                     "r.total_amount, r.status, s.slot_number " +
                     "FROM parking_records r " +
                     "JOIN parking_slots s ON r.slot_id = s.slot_id " +
                     "WHERE r.status = 'COMPLETED' " +
                     "ORDER BY r.exit_time DESC LIMIT ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit > 0 ? limit : 50);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToRecord(rs));
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * Retrieves all currently parked vehicles.
     */
    public List<ParkingRecord> getAllActiveRecords() throws SQLException {
        List<ParkingRecord> list = new ArrayList<>();
        String sql = "SELECT r.record_id, r.vehicle_number, r.vehicle_type, r.slot_id, r.entry_time, " +
                     "r.exit_time, r.duration_minutes, r.billable_hours, r.hourly_rate, r.dynamic_pricing_applied, " +
                     "r.total_amount, r.status, s.slot_number " +
                     "FROM parking_records r " +
                     "JOIN parking_slots s ON r.slot_id = s.slot_id " +
                     "WHERE r.status = 'PARKED' " +
                     "ORDER BY r.entry_time DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToRecord(rs));
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    public double getTodayRevenue() throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM parking_records WHERE status = 'COMPLETED'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
    }

    private ParkingRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        ParkingRecord record = new ParkingRecord();
        record.setRecordId(rs.getInt("record_id"));
        record.setVehicleNumber(rs.getString("vehicle_number"));
        record.setVehicleType(rs.getString("vehicle_type"));
        record.setSlotId(rs.getInt("slot_id"));
        record.setSlotNumber(rs.getString("slot_number"));

        Timestamp entryTs = rs.getTimestamp("entry_time");
        if (entryTs != null) {
            record.setEntryTime(entryTs.toLocalDateTime());
        }

        Timestamp exitTs = rs.getTimestamp("exit_time");
        if (exitTs != null) {
            record.setExitTime(exitTs.toLocalDateTime());
        }

        record.setDurationMinutes(rs.getInt("duration_minutes"));
        record.setBillableHours(rs.getInt("billable_hours"));
        record.setHourlyRate(rs.getDouble("hourly_rate"));
        record.setDynamicPricingApplied(rs.getBoolean("dynamic_pricing_applied"));
        record.setTotalAmount(rs.getDouble("total_amount"));
        record.setStatus(rs.getString("status"));
        return record;
    }
}
