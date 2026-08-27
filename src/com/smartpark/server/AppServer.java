package com.smartpark.server;

import com.smartpark.controller.*;
import com.smartpark.util.DBConfig;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

/**
 * AppServer is the standalone web server that hosts both the frontend UI and REST API.
 */
public class AppServer {
    private final int port;
    private HttpServer server;

    public AppServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(12));

        // Register Worker & Admin API Controllers
        server.createContext("/api/auth/login", new AuthController());
        server.createContext("/api/dashboard/stats", new DashboardController());
        server.createContext("/api/slots", new SlotController());
        server.createContext("/api/vehicle/entry", new EntryController());
        server.createContext("/api/vehicle/exit", new ExitController());
        server.createContext("/api/records/history", new HistoryController());
        server.createContext("/api/records/active", new HistoryController());

        // Register Customer / User API Controllers
        UserAuthController userAuthController = new UserAuthController();
        server.createContext("/api/user/auth/login", userAuthController);
        server.createContext("/api/user/auth/register", userAuthController);

        UserReservationController reservationController = new UserReservationController();
        server.createContext("/api/user/reservations/create", reservationController);
        server.createContext("/api/user/reservations/list", reservationController);
        server.createContext("/api/user/reservations/cancel", reservationController);

        MonthlyPassController passController = new MonthlyPassController();
        server.createContext("/api/user/passes/subscribe", passController);
        server.createContext("/api/user/passes/list", passController);

        // Register Static Web Assets Handler
        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("=========================================================");
        System.out.println("   SMART PARK - Smart Parking & Billing Management System");
        System.out.println("   Server running on:      http://localhost:" + port + "/");
        System.out.println("   Worker Login Portal:    http://localhost:" + port + "/login.html");
        System.out.println("   Worker Dashboard:       http://localhost:" + port + "/index.html");
        System.out.println("   Customer Login Portal:  http://localhost:" + port + "/user-login.html");
        System.out.println("   Customer Dashboard:     http://localhost:" + port + "/user-dashboard.html");
        System.out.println("=========================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * Static file handler serving HTML, CSS, JavaScript, and asset files from webapp/ directory.
     */
    static class StaticFileHandler implements HttpHandler {
        private static final String[] BASE_DIRS = {
            "webapp",
            "../webapp",
            "smart-parking/webapp",
            "."
        };

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.equals("/") || requestPath.isEmpty()) {
                requestPath = "/index.html";
            }

            // Prevent path traversal
            if (requestPath.contains("..")) {
                sendError(exchange, 403, "Forbidden");
                return;
            }

            File targetFile = null;
            for (String baseDir : BASE_DIRS) {
                File check = new File(baseDir, requestPath.startsWith("/") ? requestPath.substring(1) : requestPath);
                if (check.exists() && !check.isDirectory()) {
                    targetFile = check;
                    break;
                }
            }

            if (targetFile == null || !targetFile.exists() || targetFile.isDirectory()) {
                sendError(exchange, 404, "File Not Found: " + requestPath);
                return;
            }

            String contentType = determineContentType(targetFile.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");

            byte[] fileBytes = Files.readAllBytes(targetFile.toPath());
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }

        private String determineContentType(String fileName) {
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
            if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
            if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".svg")) return "image/svg+xml";
            if (lower.endsWith(".ico")) return "image/x-icon";
            if (lower.endsWith(".woff2")) return "font/woff2";
            if (lower.endsWith(".woff")) return "font/woff";
            if (lower.endsWith(".ttf")) return "font/ttf";
            return "text/plain; charset=UTF-8";
        }

        private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
            byte[] bytes = message.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
