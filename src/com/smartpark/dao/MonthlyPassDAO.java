package com.smartpark.dao;

import com.smartpark.model.MonthlyPass;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MonthlyPassDAO {

    public boolean createMonthlyPass(MonthlyPass pass) {
        String sql = "INSERT INTO monthly_passes (user_id, vehicle_number, vehicle_type, slot_id, start_date, end_date, months_paid, amount_paid, pass_code, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, pass.getUserId());
                pstmt.setString(2, pass.getVehicleNumber());
                pstmt.setString(3, pass.getVehicleType());
                pstmt.setInt(4, pass.getSlotId());
                pstmt.setDate(5, pass.getStartDate());
                pstmt.setDate(6, pass.getEndDate());
                pstmt.setInt(7, pass.getMonthsPaid());
                pstmt.setDouble(8, pass.getAmountPaid());
                pstmt.setString(9, pass.getPassCode());

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            pass.setPassId(rs.getInt(1));
                        }
                    }

                    // Set slot status to MONTHLY_PASS
                    String updateSlotSql = "UPDATE parking_slots SET status = 'MONTHLY_PASS' WHERE slot_id = ?";
                    try (PreparedStatement slotPstmt = conn.prepareStatement(updateSlotSql)) {
                        slotPstmt.setInt(1, pass.getSlotId());
                        slotPstmt.executeUpdate();
                    }

                    conn.commit();
                    return true;
                }
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception e) {}
            }
        }
        return false;
    }

    public List<MonthlyPass> getPassesByUserId(int userId) {
        List<MonthlyPass> list = new ArrayList<>();
        String sql = "SELECT p.*, s.slot_number FROM monthly_passes p JOIN parking_slots s ON p.slot_id = s.slot_id WHERE p.user_id = ? ORDER BY p.pass_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToMonthlyPass(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public MonthlyPass getActivePassByVehicle(String vehicleNumber) {
        String sql = "SELECT p.*, s.slot_number FROM monthly_passes p JOIN parking_slots s ON p.slot_id = s.slot_id WHERE UPPER(p.vehicle_number) = UPPER(?) AND p.status = 'ACTIVE' AND p.end_date >= CURRENT_DATE LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vehicleNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMonthlyPass(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private MonthlyPass mapRowToMonthlyPass(ResultSet rs) throws SQLException {
        MonthlyPass p = new MonthlyPass();
        p.setPassId(rs.getInt("pass_id"));
        p.setUserId(rs.getInt("user_id"));
        p.setVehicleNumber(rs.getString("vehicle_number"));
        p.setVehicleType(rs.getString("vehicle_type"));
        p.setSlotId(rs.getInt("slot_id"));
        try {
            p.setSlotNumber(rs.getString("slot_number"));
        } catch (SQLException e) {
            p.setSlotNumber("Slot " + p.getSlotId());
        }
        p.setStartDate(rs.getDate("start_date"));
        p.setEndDate(rs.getDate("end_date"));
        p.setMonthsPaid(rs.getInt("months_paid"));
        p.setAmountPaid(rs.getDouble("amount_paid"));
        p.setPassCode(rs.getString("pass_code"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    }
}
