package com.smartpark.controller;

import com.smartpark.dao.SavedVehicleDAO;
import com.smartpark.model.SavedVehicle;
import com.smartpark.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * UserVehicleController manages customer saved vehicle wallet.
 * Endpoints:
 *   GET  /api/user/vehicles/list
 *   POST /api/user/vehicles/add
 *   POST /api/user/vehicles/delete
 */
public class UserVehicleController implements HttpHandler {
    private final SavedVehicleDAO vehicleDAO = new SavedVehicleDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("GET".equalsIgnoreCase(method) && path.endsWith("/list")) {
                handleList(exchange);
            } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/add")) {
                handleAdd(exchange);
            } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/delete")) {
                handleDelete(exchange);
            } else {
                sendJsonResponse(exchange, 404, Map.of("success", false, "message", "Endpoint Not Found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, Map.of("success", false, "message", "Server Error: " + e.getMessage()));
        }
    }

    private void handleList(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        int userId = -1;
        if (query != null && query.contains("userId=")) {
            for (String param : query.split("&")) {
                if (param.startsWith("userId=")) {
                    userId = Integer.parseInt(param.substring("userId=".length()));
                }
            }
        }

        if (userId == -1) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "message", "userId parameter is required."));
            return;
        }

        List<SavedVehicle> list = vehicleDAO.getVehiclesByUserId(userId);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (SavedVehicle v : list) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("vehicleId", v.getVehicleId());
            map.put("vehicleNumber", v.getVehicleNumber());
            map.put("vehicleType", v.getVehicleType());
            resultList.add(map);
        }

        sendJsonResponse(exchange, 200, Map.of("success", true, "vehicles", resultList));
    }

    private void handleAdd(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            body.append(line);
        }

        Map<String, String> data = JsonUtil.parseObject(body.toString());
        String userIdStr = data.get("userId");
        String vehicleNumber = data.get("vehicleNumber");
        String vehicleType = data.get("vehicleType");

        if (userIdStr == null || vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "message", "userId and vehicleNumber are required."));
            return;
        }

        SavedVehicle v = new SavedVehicle();
        v.setUserId(Integer.parseInt(userIdStr));
        v.setVehicleNumber(vehicleNumber.trim().toUpperCase());
        v.setVehicleType(vehicleType != null && vehicleType.equalsIgnoreCase("2W") ? "2W" : "4W");

        boolean success = vehicleDAO.addVehicle(v);
        if (success) {
            sendJsonResponse(exchange, 201, Map.of("success", true, "message", "Vehicle saved to wallet successfully!"));
        } else {
            sendJsonResponse(exchange, 500, Map.of("success", false, "message", "Failed to save vehicle."));
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            body.append(line);
        }

        Map<String, String> data = JsonUtil.parseObject(body.toString());
        String vehicleIdStr = data.get("vehicleId");
        String userIdStr = data.get("userId");

        if (vehicleIdStr == null || userIdStr == null) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "message", "vehicleId and userId are required."));
            return;
        }

        boolean success = vehicleDAO.deleteVehicle(Integer.parseInt(vehicleIdStr), Integer.parseInt(userIdStr));
        if (success) {
            sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Vehicle removed from wallet."));
        } else {
            sendJsonResponse(exchange, 400, Map.of("success", false, "message", "Failed to delete vehicle."));
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
