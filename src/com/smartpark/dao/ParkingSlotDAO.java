package com.smartpark.dao;

import com.smartpark.model.ParkingSlot;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ParkingSlotDAO handles slot queries, state changes, and occupancy calculations.
 */
public class ParkingSlotDAO {

    /**
     * Retrieves all parking slots ordered by slot number.
     */
    public List<ParkingSlot> getAllSlots() throws SQLException {
        List<ParkingSlot> list = new ArrayList<>();
        String sql = "SELECT slot_id, slot_number, vehicle_type, status, floor_level FROM parking_slots ORDER BY slot_number ASC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ParkingSlot slot = mapResultSetToSlot(rs);
                list.add(slot);
            }
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * Finds the first available slot for a given vehicle category (2W or 4W).
     */
    public ParkingSlot getAvailableSlot(String vehicleType) throws SQLException {
        String sql = "SELECT slot_id, slot_number, vehicle_type, status, floor_level FROM parking_slots " +
                     "WHERE vehicle_type = ? AND status = 'AVAILABLE' ORDER BY slot_id ASC LIMIT 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, vehicleType);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToSlot(rs);
            }
            return null;
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
    }

    /**
     * Retrieves a slot by its primary key.
     */
    public ParkingSlot getSlotById(int slotId) throws SQLException {
        String sql = "SELECT slot_id, slot_number, vehicle_type, status, floor_level FROM parking_slots WHERE slot_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, slotId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToSlot(rs);
            }
            return null;
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
    }

    /**
     * Updates the status of a parking slot (e.g. AVAILABLE, OCCUPIED, RESERVED).
     */
    public boolean updateSlotStatus(int slotId, String status) throws SQLException {
        String sql = "UPDATE parking_slots SET status = ? WHERE slot_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, slotId);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } finally {
            DBConnection.close(conn, pstmt);
        }
    }

    /**
     * Updates the status using an existing connection (for transactional operations).
     */
    public boolean updateSlotStatus(Connection conn, int slotId, String status) throws SQLException {
        String sql = "UPDATE parking_slots SET status = ? WHERE slot_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, slotId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public int getTotalCount() throws SQLException {
        return getCountBySql("SELECT COUNT(*) FROM parking_slots");
    }

    public int getOccupiedCount() throws SQLException {
        return getCountBySql("SELECT COUNT(*) FROM parking_slots WHERE status = 'OCCUPIED'");
    }

    public int getAvailableCount() throws SQLException {
        return getCountBySql("SELECT COUNT(*) FROM parking_slots WHERE status = 'AVAILABLE'");
    }

    public int getReservedCount() throws SQLException {
        return getCountBySql("SELECT COUNT(*) FROM parking_slots WHERE status = 'RESERVED'");
    }

    public int getMaintenanceCount() throws SQLException {
        return getCountBySql("SELECT COUNT(*) FROM parking_slots WHERE status = 'MAINTENANCE'");
    }

    public boolean toggleSlotMaintenance(int slotId) throws SQLException {
        ParkingSlot slot = getSlotById(slotId);
        if (slot == null) return false;
        String newStatus = "MAINTENANCE".equalsIgnoreCase(slot.getStatus()) ? "AVAILABLE" : "MAINTENANCE";
        return updateSlotStatus(slotId, newStatus);
    }

    public int getCountByTypeAndStatus(String vehicleType, String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_slots WHERE vehicle_type = ? AND status = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, vehicleType);
            pstmt.setString(2, status);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
    }

    public int getTotalCountByType(String vehicleType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_slots WHERE vehicle_type = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, vehicleType);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
    }

    private int getCountBySql(String sql) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
    }

    private ParkingSlot mapResultSetToSlot(ResultSet rs) throws SQLException {
        ParkingSlot slot = new ParkingSlot();
        slot.setSlotId(rs.getInt("slot_id"));
        slot.setSlotNumber(rs.getString("slot_number"));
        slot.setVehicleType(rs.getString("vehicle_type"));
        slot.setStatus(rs.getString("status"));
        slot.setFloorLevel(rs.getInt("floor_level"));
        return slot;
    }
}
