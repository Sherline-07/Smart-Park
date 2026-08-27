package com.smartpark.controller;

import com.smartpark.model.ParkingRecord;
import com.smartpark.service.ParkingService;
import com.smartpark.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HistoryController serves completed parking transactions and active parked vehicle list.
 * Endpoints:
 * - GET /api/records/history
 * - GET /api/records/active
 */
public class HistoryController implements HttpHandler {
    private final ParkingService parkingService = new ParkingService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJsonResponse(exchange, 405, Map.of("success", false, "message", "Method Not Allowed"));
            return;
        }

        String path = exchange.getRequestURI().getPath();
        try {
            if (path.contains("/active")) {
                List<ParkingRecord> activeRecords = parkingService.getActiveRecords();
                List<Map<String, Object>> listMap = new ArrayList<>();
                for (ParkingRecord r : activeRecords) {
                    listMap.add(r.toMap());
                }
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", true);
                response.put("count", listMap.size());
                response.put("records", listMap);
                sendJsonResponse(exchange, 200, response);
            } else {
                List<ParkingRecord> history = parkingService.getRecentHistory(100);
                List<Map<String, Object>> listMap = new ArrayList<>();
                for (ParkingRecord r : history) {
                    listMap.add(r.toMap());
                }
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", true);
                response.put("count", listMap.size());
                response.put("records", listMap);
                sendJsonResponse(exchange, 200, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, Map.of("success", false, "message", "Error fetching records: " + e.getMessage()));
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
