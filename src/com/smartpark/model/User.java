package com.smartpark.model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String defaultVehicleNumber;
    private String defaultVehicleType;
    private Timestamp createdAt;

    public User() {}

    public User(int userId, String username, String password, String fullName, String email, String phone, String defaultVehicleNumber, String defaultVehicleType) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.defaultVehicleNumber = defaultVehicleNumber;
        this.defaultVehicleType = defaultVehicleType;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDefaultVehicleNumber() { return defaultVehicleNumber; }
    public void setDefaultVehicleNumber(String defaultVehicleNumber) { this.defaultVehicleNumber = defaultVehicleNumber; }

    public String getDefaultVehicleType() { return defaultVehicleType; }
    public void setDefaultVehicleType(String defaultVehicleType) { this.defaultVehicleType = defaultVehicleType; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
