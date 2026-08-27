-- =======================================================
-- SMART PARK: Smart Parking & Billing Management System
-- Database Setup Script (MySQL)
-- =======================================================

-- 1. Create Database
CREATE DATABASE IF NOT EXISTS smart_parking;
USE smart_parking;

-- 2. Drop existing tables if they exist (in reverse FK order)
DROP TABLE IF EXISTS parking_records;
DROP TABLE IF EXISTS parking_slots;
DROP TABLE IF EXISTS workers;

-- -------------------------------------------------------
-- 3. Table: workers (Worker/Admin Accounts)
-- -------------------------------------------------------
CREATE TABLE workers (
    worker_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'WORKER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 4. Table: parking_slots (Parking Slots Grid)
-- -------------------------------------------------------
CREATE TABLE parking_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    slot_number VARCHAR(20) NOT NULL UNIQUE,
    vehicle_type ENUM('2W', '4W') NOT NULL,
    status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED', 'MONTHLY_PASS') DEFAULT 'AVAILABLE',
    floor_level INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 5. Table: users (Customer Accounts)
-- -------------------------------------------------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NULL,
    phone VARCHAR(20) NULL,
    default_vehicle_number VARCHAR(20) NULL,
    default_vehicle_type ENUM('2W', '4W') DEFAULT '4W',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 6. Table: reservations (User Advance Pre-Bookings)
-- -------------------------------------------------------
CREATE TABLE reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    vehicle_number VARCHAR(20) NOT NULL,
    vehicle_type ENUM('2W', '4W') NOT NULL,
    slot_id INT NOT NULL,
    scheduled_entry DATETIME NOT NULL,
    duration_hours INT NOT NULL DEFAULT 2,
    estimated_fee DECIMAL(8,2) NOT NULL,
    pass_code VARCHAR(30) NOT NULL UNIQUE,
    status ENUM('CONFIRMED', 'CHECKED_IN', 'CANCELLED', 'EXPIRED') DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_res_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_res_slot FOREIGN KEY (slot_id) REFERENCES parking_slots(slot_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 7. Table: monthly_passes (Daily Commuter Long-Term Subscriptions)
-- -------------------------------------------------------
CREATE TABLE monthly_passes (
    pass_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    vehicle_number VARCHAR(20) NOT NULL,
    vehicle_type ENUM('2W', '4W') NOT NULL,
    slot_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    months_paid INT NOT NULL DEFAULT 1,
    amount_paid DECIMAL(10,2) NOT NULL,
    pass_code VARCHAR(30) NOT NULL UNIQUE,
    status ENUM('ACTIVE', 'EXPIRED', 'CANCELLED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pass_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_pass_slot FOREIGN KEY (slot_id) REFERENCES parking_slots(slot_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 8. Table: parking_records (Parking Activity & Transactions)
-- -------------------------------------------------------
CREATE TABLE parking_records (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(20) NOT NULL,
    vehicle_type ENUM('2W', '4W') NOT NULL,
    slot_id INT NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME NULL,
    duration_minutes INT NULL,
    billable_hours INT NULL,
    hourly_rate DECIMAL(8,2) NULL,
    dynamic_pricing_applied BOOLEAN DEFAULT FALSE,
    total_amount DECIMAL(8,2) NULL,
    status ENUM('PARKED', 'COMPLETED', 'CANCELLED') DEFAULT 'PARKED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_slot FOREIGN KEY (slot_id) REFERENCES parking_slots(slot_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- 9. Seed Initial Data
-- -------------------------------------------------------

-- Seed default user
INSERT INTO users (username, password, full_name, email, phone, default_vehicle_number, default_vehicle_type) VALUES
('user1', 'user123', 'Anand Verma', 'anand@example.com', '9876543210', 'TN01AB9999', '4W'),
('user2', 'user123', 'Priya Sharma', 'priya@example.com', '9876543211', 'TN02CD8888', '2W');


-- Insert Default Worker and Admin accounts
INSERT INTO workers (username, password, full_name, role) VALUES
('admin', 'admin123', 'System Administrator', 'ADMIN'),
('worker1', 'worker123', 'Ramesh Kumar (Operator)', 'WORKER'),
('worker2', 'worker123', 'Suresh Raina (Operator)', 'WORKER');

-- Insert Initial Parking Slots
-- 15 Four-Wheeler (4W) Slots across Floor 1 & Floor 2
INSERT INTO parking_slots (slot_number, vehicle_type, status, floor_level) VALUES
('A-01', '4W', 'AVAILABLE', 1),
('A-02', '4W', 'AVAILABLE', 1),
('A-03', '4W', 'AVAILABLE', 1),
('A-04', '4W', 'AVAILABLE', 1),
('A-05', '4W', 'AVAILABLE', 1),
('A-06', '4W', 'AVAILABLE', 1),
('A-07', '4W', 'AVAILABLE', 1),
('A-08', '4W', 'AVAILABLE', 1),
('A-09', '4W', 'AVAILABLE', 2),
('A-10', '4W', 'AVAILABLE', 2),
('A-11', '4W', 'AVAILABLE', 2),
('A-12', '4W', 'AVAILABLE', 2),
('A-13', '4W', 'AVAILABLE', 2),
('A-14', '4W', 'AVAILABLE', 2),
('A-15', '4W', 'AVAILABLE', 2);

-- 15 Two-Wheeler (2W) Slots across Floor 1 & Floor 2
INSERT INTO parking_slots (slot_number, vehicle_type, status, floor_level) VALUES
('B-01', '2W', 'AVAILABLE', 1),
('B-02', '2W', 'AVAILABLE', 1),
('B-03', '2W', 'AVAILABLE', 1),
('B-04', '2W', 'AVAILABLE', 1),
('B-05', '2W', 'AVAILABLE', 1),
('B-06', '2W', 'AVAILABLE', 1),
('B-07', '2W', 'AVAILABLE', 1),
('B-08', '2W', 'AVAILABLE', 1),
('B-09', '2W', 'AVAILABLE', 2),
('B-10', '2W', 'AVAILABLE', 2),
('B-11', '2W', 'AVAILABLE', 2),
('B-12', '2W', 'AVAILABLE', 2),
('B-13', '2W', 'AVAILABLE', 2),
('B-14', '2W', 'AVAILABLE', 2),
('B-15', '2W', 'AVAILABLE', 2);
