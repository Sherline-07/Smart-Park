package com.smartpark.controller;

import com.smartpark.dao.ParkingSlotDAO;
import com.smartpark.model.ParkingSlot;
import com.smartpark.service.ParkingService;
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
 * SlotController retrieves parking slots and handles slot maintenance mode toggles.
 * Endpoints:
 *   GET  /api/slots
 *   POST /api/slots/maintenance
 */
public class SlotController implements HttpHandler {
    private final ParkingService parkingService = new ParkingService();
    private final ParkingSlotDAO slotDAO = new ParkingSlotDAO();

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
            if ("POST".equalsIgnoreCase(method) && path.endsWith("/maintenance")) {
                handleMaintenanceToggle(exchange);
            } else if ("GET".equalsIgnoreCase(method)) {
                handleGetSlots(exchange);
            } else {
                sendJsonResponse(exchange, 405, Map.of("success", false, "message", "Method Not Allowed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, Map.of("success", false, "message", "Error handling slots request: " + e.getMessage()));
        }
    }

    private void handleGetSlots(HttpExchange exchange) throws Exception {
        List<ParkingSlot> slots = parkingService.getAllSlots();
        List<Map<String, Object>> slotsMap = new ArrayList<>();
        for (ParkingSlot slot : slots) {
            slotsMap.add(slot.toMap());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("count", slots.size());
        response.put("slots", slotsMap);
        sendJsonResponse(exchange, 200, response);
    }

    private void handleMaintenanceToggle(HttpExchange exchange) throws Exception {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            body.append(line);
        }

        Map<String, String> data = JsonUtil.parseObject(body.toString());
        String slotIdStr = data.get("slotId");

        if (slotIdStr == null || slotIdStr.trim().isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "message", "slotId is required."));
            return;
        }

        int slotId = Integer.parseInt(slotIdStr);
        boolean success = slotDAO.toggleSlotMaintenance(slotId);
        if (success) {
            ParkingSlot updated = slotDAO.getSlotById(slotId);
            sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Slot " + updated.getSlotNumber() + " maintenance status toggled to: " + updated.getStatus(), "status", updated.getStatus()));
        } else {
            sendJsonResponse(exchange, 400, Map.of("success", false, "message", "Unable to toggle slot maintenance."));
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
