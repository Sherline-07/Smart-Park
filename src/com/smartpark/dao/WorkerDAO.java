package com.smartpark.dao;

import com.smartpark.model.Worker;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * WorkerDAO handles worker login and authentication queries.
 */
public class WorkerDAO {

    /**
     * Authenticates worker credentials against the database.
     * Uses PreparedStatement to prevent SQL injection.
     */
    public Worker authenticate(String username, String password) throws SQLException {
        String sql = "SELECT worker_id, username, password, full_name, role FROM workers WHERE username = ? AND password = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Worker worker = new Worker();
                worker.setWorkerId(rs.getInt("worker_id"));
                worker.setUsername(rs.getString("username"));
                worker.setPassword(rs.getString("password"));
                worker.setFullName(rs.getString("full_name"));
                worker.setRole(rs.getString("role"));
                return worker;
            }
            return null;
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
    }
}
