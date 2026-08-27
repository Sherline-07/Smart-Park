package com.smartpark.controller;

import com.smartpark.model.BillingReceipt;
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
 * ExitController handles vehicle checkout, dynamic fee calculation, and invoice generation.
 * Endpoint: POST /api/vehicle/exit
 */
public class ExitController implements HttpHandler {
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

            if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
                sendJsonResponse(exchange, 400, Map.of("success", false, "message", "Vehicle number is required."));
                return;
            }

            BillingReceipt receipt = parkingService.processExit(vehicleNumber.trim());
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Vehicle exited successfully! Bill generated.");
            response.put("receipt", receipt.toMap());
            sendJsonResponse(exchange, 200, response);

        } catch (IllegalArgumentException e) {
            sendJsonResponse(exchange, 404, Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, Map.of("success", false, "message", "Internal Error: " + e.getMessage()));
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
