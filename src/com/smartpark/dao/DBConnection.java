package com.smartpark.dao;

import com.smartpark.util.DBConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DBConnection provides JDBC database connectivity using PreparedStatement.
 * Primary: MySQL Database (smart_parking)
 * Fallback: Standalone / Embedded Database (for offline demos or before MySQL server is started)
 */
public class DBConnection {
    private static boolean mysqlAvailable = true;
    private static boolean fallbackInitialized = false;

    static {
        try {
            Class.forName(DBConfig.getDbDriver());
        } catch (ClassNotFoundException e) {
            System.err.println("Notice: MySQL Driver not found on default classpath.");
        }
    }

    /**
     * Obtains a valid database Connection.
     */
    public static Connection getConnection() throws SQLException {
        if (mysqlAvailable) {
            try {
                Class.forName(DBConfig.getDbDriver());
                Connection conn = DriverManager.getConnection(
                    DBConfig.getDbUrl(),
                    DBConfig.getDbUser(),
                    DBConfig.getDbPassword()
                );
                return conn;
            } catch (Exception e) {
                System.err.println("---------------------------------------------------------");
                System.err.println("MySQL Connection Note: " + e.getMessage());
                System.err.println("Activating Standalone/Embedded JDBC database mode...");
                System.err.println("---------------------------------------------------------");
                mysqlAvailable = false;
            }
        }

        // Fallback to Embedded Database if enabled
        if (DBConfig.isFallbackEnabled()) {
            try {
                Class.forName(DBConfig.getFallbackDriver());
                Connection conn = DriverManager.getConnection(
                    DBConfig.getFallbackUrl(),
                    DBConfig.getFallbackUser(),
                    DBConfig.getFallbackPassword()
                );
                
                if (!fallbackInitialized) {
                    initFallbackDatabase(conn);
                    fallbackInitialized = true;
                }
                return conn;
            } catch (Exception ex) {
                throw new SQLException("Failed to connect to both MySQL and Fallback database: " + ex.getMessage(), ex);
            }
        }

        throw new SQLException("Could not connect to MySQL database at " + DBConfig.getDbUrl());
    }

    /**
     * Initializes schema and seeds default data in fallback database.
     */
    private static synchronized void initFallbackDatabase(Connection conn) {
        System.out.println("Initializing Database tables and seed data...");
        try (Statement stmt = conn.createStatement()) {
            // Drop tables if exist
            try { stmt.execute("DROP TABLE IF EXISTS user_vehicles"); } catch (Exception e) {}
            try { stmt.execute("DROP TABLE IF EXISTS monthly_passes"); } catch (Exception e) {}
            try { stmt.execute("DROP TABLE IF EXISTS reservations"); } catch (Exception e) {}
            try { stmt.execute("DROP TABLE IF EXISTS users"); } catch (Exception e) {}
            try { stmt.execute("DROP TABLE IF EXISTS parking_records"); } catch (Exception e) {}
            try { stmt.execute("DROP TABLE IF EXISTS parking_slots"); } catch (Exception e) {}
            try { stmt.execute("DROP TABLE IF EXISTS workers"); } catch (Exception e) {}

            // 1. Create workers table
            stmt.execute("CREATE TABLE workers (" +
                         "worker_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "username VARCHAR(50) NOT NULL UNIQUE, " +
                         "password VARCHAR(100) NOT NULL, " +
                         "full_name VARCHAR(100) NOT NULL, " +
                         "role VARCHAR(20) DEFAULT 'WORKER', " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 2. Create users table
            stmt.execute("CREATE TABLE users (" +
                         "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "username VARCHAR(50) NOT NULL UNIQUE, " +
                         "password VARCHAR(100) NOT NULL, " +
                         "full_name VARCHAR(100) NOT NULL, " +
                         "email VARCHAR(100) NULL, " +
                         "phone VARCHAR(20) NULL, " +
                         "default_vehicle_number VARCHAR(20) NULL, " +
                         "default_vehicle_type VARCHAR(10) DEFAULT '4W', " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 2b. Create user_vehicles table
            stmt.execute("CREATE TABLE user_vehicles (" +
                         "vehicle_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "user_id INT NOT NULL, " +
                         "vehicle_number VARCHAR(20) NOT NULL, " +
                         "vehicle_type VARCHAR(10) DEFAULT '4W', " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE)");

            // 3. Create parking_slots table
            stmt.execute("CREATE TABLE parking_slots (" +
                         "slot_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "slot_number VARCHAR(20) NOT NULL UNIQUE, " +
                         "vehicle_type VARCHAR(10) NOT NULL, " +
                         "status VARCHAR(20) DEFAULT 'AVAILABLE', " +
                         "floor_level INT DEFAULT 1, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 4. Create reservations table
            stmt.execute("CREATE TABLE reservations (" +
                         "reservation_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "user_id INT NOT NULL, " +
                         "vehicle_number VARCHAR(20) NOT NULL, " +
                         "vehicle_type VARCHAR(10) NOT NULL, " +
                         "slot_id INT NOT NULL, " +
                         "scheduled_entry TIMESTAMP NOT NULL, " +
                         "duration_hours INT DEFAULT 2, " +
                         "estimated_fee DECIMAL(8,2) NOT NULL, " +
                         "pass_code VARCHAR(30) NOT NULL UNIQUE, " +
                         "status VARCHAR(20) DEFAULT 'CONFIRMED', " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                         "FOREIGN KEY (slot_id) REFERENCES parking_slots(slot_id) ON DELETE CASCADE)");

            // 5. Create monthly_passes table
            stmt.execute("CREATE TABLE monthly_passes (" +
                         "pass_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "user_id INT NOT NULL, " +
                         "vehicle_number VARCHAR(20) NOT NULL, " +
                         "vehicle_type VARCHAR(10) NOT NULL, " +
                         "slot_id INT NOT NULL, " +
                         "start_date DATE NOT NULL, " +
                         "end_date DATE NOT NULL, " +
                         "months_paid INT DEFAULT 1, " +
                         "amount_paid DECIMAL(10,2) NOT NULL, " +
                         "pass_code VARCHAR(30) NOT NULL UNIQUE, " +
                         "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                         "FOREIGN KEY (slot_id) REFERENCES parking_slots(slot_id) ON DELETE CASCADE)");

            // 6. Create parking_records table
            stmt.execute("CREATE TABLE parking_records (" +
                         "record_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "vehicle_number VARCHAR(20) NOT NULL, " +
                         "vehicle_type VARCHAR(10) NOT NULL, " +
                         "slot_id INT NOT NULL, " +
                         "entry_time TIMESTAMP NOT NULL, " +
                         "exit_time TIMESTAMP NULL, " +
                         "duration_minutes INT NULL, " +
                         "billable_hours INT NULL, " +
                         "hourly_rate DECIMAL(8,2) NULL, " +
                         "dynamic_pricing_applied BOOLEAN DEFAULT FALSE, " +
                         "total_amount DECIMAL(8,2) NULL, " +
                         "status VARCHAR(20) DEFAULT 'PARKED', " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "FOREIGN KEY (slot_id) REFERENCES parking_slots(slot_id) ON DELETE CASCADE)");

            // Seed Worker accounts
            stmt.execute("INSERT INTO workers (username, password, full_name, role) VALUES " +
                         "('admin', 'admin123', 'System Administrator', 'ADMIN'), " +
                         "('worker1', 'worker123', 'Ramesh Kumar (Operator)', 'WORKER'), " +
                         "('worker2', 'worker123', 'Suresh Raina (Operator)', 'WORKER')");

            // Seed User accounts
            stmt.execute("INSERT INTO users (username, password, full_name, email, phone, default_vehicle_number, default_vehicle_type) VALUES " +
                         "('user1', 'user123', 'Anand Verma', 'anand@example.com', '9876543210', 'TN01AB9999', '4W'), " +
                         "('user2', 'user123', 'Priya Sharma', 'priya@example.com', '9876543211', 'TN02CD8888', '2W')");

            // Seed 15 4W Slots
            for (int i = 1; i <= 15; i++) {
                String slotNum = String.format("A-%02d", i);
                int floor = i <= 8 ? 1 : 2;
                stmt.execute(String.format("INSERT INTO parking_slots (slot_number, vehicle_type, status, floor_level) VALUES ('%s', '4W', 'AVAILABLE', %d)", slotNum, floor));
            }

            // Seed 15 2W Slots
            for (int i = 1; i <= 15; i++) {
                String slotNum = String.format("B-%02d", i);
                int floor = i <= 8 ? 1 : 2;
                stmt.execute(String.format("INSERT INTO parking_slots (slot_number, vehicle_type, status, floor_level) VALUES ('%s', '2W', 'AVAILABLE', %d)", slotNum, floor));
            }

            System.out.println("Database schema and 30 slots initialized successfully.");
        } catch (Exception e) {
            System.err.println("Error initializing DB schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (Exception e) {}
        try { if (stmt != null) stmt.close(); } catch (Exception e) {}
        try { if (conn != null) conn.close(); } catch (Exception e) {}
    }

    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }
}
