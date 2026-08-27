package com.smartpark.dao;

import com.smartpark.model.Reservation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public boolean createReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (user_id, vehicle_number, vehicle_type, slot_id, scheduled_entry, duration_hours, estimated_fee, pass_code, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMED')";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, reservation.getUserId());
                pstmt.setString(2, reservation.getVehicleNumber());
                pstmt.setString(3, reservation.getVehicleType());
                pstmt.setInt(4, reservation.getSlotId());
                pstmt.setTimestamp(5, reservation.getScheduledEntry());
                pstmt.setInt(6, reservation.getDurationHours());
                pstmt.setDouble(7, reservation.getEstimatedFee());
                pstmt.setString(8, reservation.getPassCode());
                
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            reservation.setReservationId(rs.getInt(1));
                        }
                    }
                    
                    // Mark slot status as RESERVED
                    String updateSlotSql = "UPDATE parking_slots SET status = 'RESERVED' WHERE slot_id = ?";
                    try (PreparedStatement slotPstmt = conn.prepareStatement(updateSlotSql)) {
                        slotPstmt.setInt(1, reservation.getSlotId());
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

    public List<Reservation> getReservationsByUserId(int userId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, s.slot_number FROM reservations r JOIN parking_slots s ON r.slot_id = s.slot_id WHERE r.user_id = ? ORDER BY r.reservation_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToReservation(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Reservation getByPassCodeOrVehicle(String codeOrPlate) {
        String sql = "SELECT r.*, s.slot_number FROM reservations r JOIN parking_slots s ON r.slot_id = s.slot_id WHERE (r.pass_code = ? OR UPPER(r.vehicle_number) = UPPER(?)) AND r.status = 'CONFIRMED' ORDER BY r.reservation_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codeOrPlate);
            pstmt.setString(2, codeOrPlate);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToReservation(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStatus(int reservationId, String newStatus) {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, reservationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cancelReservation(int reservationId, int userId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String getSql = "SELECT slot_id FROM reservations WHERE reservation_id = ? AND user_id = ? AND status = 'CONFIRMED'";
            int slotId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(getSql)) {
                pstmt.setInt(1, reservationId);
                pstmt.setInt(2, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        slotId = rs.getInt("slot_id");
                    }
                }
            }

            if (slotId != -1) {
                String updateRes = "UPDATE reservations SET status = 'CANCELLED' WHERE reservation_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateRes)) {
                    pstmt.setInt(1, reservationId);
                    pstmt.executeUpdate();
                }

                String updateSlot = "UPDATE parking_slots SET status = 'AVAILABLE' WHERE slot_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateSlot)) {
                    pstmt.setInt(1, slotId);
                    pstmt.executeUpdate();
                }

                conn.commit();
                return true;
            }
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) {}
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception e) {}
            }
        }
        return false;
    }

    private Reservation mapRowToReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId(rs.getInt("reservation_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setVehicleNumber(rs.getString("vehicle_number"));
        r.setVehicleType(rs.getString("vehicle_type"));
        r.setSlotId(rs.getInt("slot_id"));
        try {
            r.setSlotNumber(rs.getString("slot_number"));
        } catch (SQLException e) {
            r.setSlotNumber("Slot " + r.getSlotId());
        }
        r.setScheduledEntry(rs.getTimestamp("scheduled_entry"));
        r.setDurationHours(rs.getInt("duration_hours"));
        r.setEstimatedFee(rs.getDouble("estimated_fee"));
        r.setPassCode(rs.getString("pass_code"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        return r;
    }
}
