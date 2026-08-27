package com.smartpark.dao;

import com.smartpark.model.SavedVehicle;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SavedVehicleDAO {

    public boolean addVehicle(SavedVehicle vehicle) {
        String sql = "INSERT INTO user_vehicles (user_id, vehicle_number, vehicle_type) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehicle.getUserId());
            pstmt.setString(2, vehicle.getVehicleNumber().toUpperCase());
            pstmt.setString(3, vehicle.getVehicleType().toUpperCase());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<SavedVehicle> getVehiclesByUserId(int userId) {
        List<SavedVehicle> list = new ArrayList<>();
        String sql = "SELECT * FROM user_vehicles WHERE user_id = ? ORDER BY vehicle_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SavedVehicle v = new SavedVehicle();
                    v.setVehicleId(rs.getInt("vehicle_id"));
                    v.setUserId(rs.getInt("user_id"));
                    v.setVehicleNumber(rs.getString("vehicle_number"));
                    v.setVehicleType(rs.getString("vehicle_type"));
                    v.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(v);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteVehicle(int vehicleId, int userId) {
        String sql = "DELETE FROM user_vehicles WHERE vehicle_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehicleId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
