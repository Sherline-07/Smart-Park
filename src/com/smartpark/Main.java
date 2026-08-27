package com.smartpark;

import com.smartpark.server.AppServer;
import com.smartpark.util.DBConfig;

/**
 * Main entry point for the Smart Park Worker/Admin Backend Application.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Smart Park Management System...");
        int port = DBConfig.getServerPort();
        try {
            AppServer server = new AppServer(port);
            server.start();
        } catch (Exception e) {
            System.err.println("Fatal: Failed to launch server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
