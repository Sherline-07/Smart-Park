package com.smartpark.controller;

import com.smartpark.dao.UserDAO;
import com.smartpark.model.User;
import com.smartpark.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * UserAuthController handles User sign-up and login requests.
 * Endpoints:
 *   POST /api/user/auth/login
 *   POST /api/user/auth/register
 */
public class UserAuthController implements HttpHandler {
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJsonResponse(exchange, 405, Map.of("success", false, "message", "Method Not Allowed"));
            return;
        }

        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                body.append(line);
            }

            Map<String, String> data = JsonUtil.parseObject(body.toString());

            if (path.endsWith("/login")) {
                handleLogin(exchange, data);
            } else if (path.endsWith("/register")) {
                handleRegister(exchange, data);
            } else {
                sendJsonResponse(exchange, 404, Map.of("success", false, "message", "Endpoint Not Found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, Map.of("success", false, "message", "Server Error: " + e.getMessage()));
        }
    }

    private void handleLogin(HttpExchange exchange, Map<String, String> data) throws IOException {
        String username = data.get("username");
        String password = data.get("password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "message", "Username and password are required."));
            return;
        }

        User user = userDAO.authenticate(username.trim(), password.trim());
        if (user != null) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "User login successful!");
            Map<String, Object> userMap = new LinkedHashMap<>();
            userMap.put("userId", user.getUserId());
            userMap.put("username", user.getUsername());
            userMap.put("fullName", user.getFullName());
            userMap.put("email", user.getEmail());
            userMap.put("phone", user.getPhone());
            userMap.put("defaultVehicleNumber", user.getDefaultVehicleNumber());
            userMap.put("defaultVehicleType", user.getDefaultVehicleType());
            response.put("user", userMap);
            sendJsonResponse(exchange, 200, response);
        } else {
            sendJsonResponse(exchange, 401, Map.of("success", false, "message", "Invalid username or password."));
        }
    }

    private void handleRegister(HttpExchange exchange, Map<String, String> data) throws IOException {
        String username = data.get("username");
        String password = data.get("password");
        String fullName = data.get("fullName");
        String email = data.get("email");
        String phone = data.get("phone");
        String defaultVehicleNumber = data.get("defaultVehicleNumber");
        String defaultVehicleType = data.get("defaultVehicleType");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty() || fullName == null || fullName.trim().isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "message", "Full Name, Username, and Password are required."));
            return;
        }

        if (userDAO.isUsernameTaken(username.trim())) {
            sendJsonResponse(exchange, 409, Map.of("success", false, "message", "Username '" + username.trim() + "' is already registered."));
            return;
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password.trim());
        user.setFullName(fullName.trim());
        user.setEmail(email != null ? email.trim() : "");
        user.setPhone(phone != null ? phone.trim() : "");
        user.setDefaultVehicleNumber(defaultVehicleNumber != null ? defaultVehicleNumber.trim().toUpperCase() : "");
        user.setDefaultVehicleType(defaultVehicleType != null && defaultVehicleType.equalsIgnoreCase("2W") ? "2W" : "4W");

        boolean success = userDAO.register(user);
        if (success) {
            sendJsonResponse(exchange, 201, Map.of("success", true, "message", "Registration successful! You can now log in."));
        } else {
            sendJsonResponse(exchange, 500, Map.of("success", false, "message", "Failed to register user."));
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = JsonUtil.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
