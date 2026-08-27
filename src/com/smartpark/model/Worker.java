package com.smartpark.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Worker/Admin Entity representing staff accounts in the system.
 */
public class Worker {
    private int workerId;
    private String username;
    private String password;
    private String fullName;
    private String role; // "ADMIN" or "WORKER"

    public Worker() {}

    public Worker(int workerId, String username, String password, String fullName, String role) {
        this.workerId = workerId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public int getWorkerId() { return workerId; }
    public void setWorkerId(int workerId) { this.workerId = workerId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("workerId", workerId);
        map.put("username", username);
        map.put("fullName", fullName);
        map.put("role", role);
        return map;
    }
}
