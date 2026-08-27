package com.smartpark.controller;

import com.smartpark.service.ParkingService;
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
 * EntryController handles vehicle check-in and automated slot assignment.
 * Endpoint: POST /api/vehicle/entry
 */
public class EntryController implements HttpHandler {
    private final ParkingService parkingService = new ParkingService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

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
            String vehicleNumber = data.get("vehicleNumber");
            String vehicleType = data.get("vehicleType");

            if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
                sendJsonResponse(exchange, 400, Map.of("success", false, "message", "Vehicle number is required."));
                return;
            }

            if (vehicleType == null || vehicleType.trim().isEmpty()) {
                sendJsonResponse(exchange, 400, Map.of("success", false, "message", "Vehicle category (2W or 4W) is required."));
                return;
            }

            Map<String, Object> entryResult = parkingService.processEntry(vehicleNumber.trim(), vehicleType.trim());
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", entryResult.get("message"));
            response.put("data", entryResult);
            sendJsonResponse(exchange, 200, response);

        } catch (IllegalArgumentException e) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalStateException e) {
            sendJsonResponse(exchange, 409, Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, Map.of("success", false, "message", "Internal error: " + e.getMessage()));
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
