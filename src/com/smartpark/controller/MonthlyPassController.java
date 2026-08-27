package com.smartpark.controller;

import com.smartpark.dao.MonthlyPassDAO;
import com.smartpark.dao.ParkingSlotDAO;
import com.smartpark.model.MonthlyPass;
import com.smartpark.model.ParkingSlot;
import com.smartpark.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * MonthlyPassController manages daily commuter long-term parking pass subscriptions.
 * Endpoints:
 *   POST /api/user/passes/subscribe
 *   GET  /api/user/passes/list
 */
public class MonthlyPassController implements HttpHandler {
    private final MonthlyPassDAO passDAO = new MonthlyPassDAO();
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
            if ("GET".equalsIgnoreCase(method) && path.endsWith("/list")) {
                handleList(exchange);
            } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/subscribe")) {
                handleSubscribe(exchange);
            } else {
                sendJsonResponse(exchange, 404, Map.of("success", false, "message", "Endpoint Not Found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, Map.of("success", false, "message", "Server Error: " + e.getMessage()));
        }
    }

    private void handleSubscribe(HttpExchange exchange) throws IOException {
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
        String monthsStr = data.get("monthsPaid");
        String slotIdStr = data.get("slotId");

        if (userIdStr == null || vehicleNumber == null || vehicleType == null) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "message", "userId, vehicle number, and vehicle type are required."));
            return;
        }

        int userId = Integer.parseInt(userIdStr);
        vehicleNumber = vehicleNumber.trim().toUpperCase();
        vehicleType = vehicleType.trim().toUpperCase();
        int monthsPaid = monthsStr != null ? Integer.parseInt(monthsStr) : 1;

        int slotId = -1;
        ParkingSlot targetSlot = null;

        try {
            if (slotIdStr != null && !slotIdStr.trim().isEmpty()) {
                slotId = Integer.parseInt(slotIdStr);
                targetSlot = slotDAO.getSlotById(slotId);
                if (targetSlot == null || !"AVAILABLE".equalsIgnoreCase(targetSlot.getStatus())) {
                    sendJsonResponse(exchange, 400, Map.of("success", false, "message", "Selected slot is not available for monthly reservation."));
                    return;
                }
            } else {
                targetSlot = slotDAO.getAvailableSlot(vehicleType);
                if (targetSlot == null) {
                    sendJsonResponse(exchange, 400, Map.of("success", false, "message", "No available parking slot for " + vehicleType + " monthly pass."));
                    return;
                }
                slotId = targetSlot.getSlotId();
            }

            // Monthly rates (INR): 4W = ₹2,500/month, 2W = ₹1,200/month
            double baseMonthlyRate = vehicleType.equalsIgnoreCase("4W") ? 2500.0 : 1200.0;
            double totalAmount = baseMonthlyRate * monthsPaid;

            // Apply 10% discount for 3+ months subscription
            if (monthsPaid >= 3) {
                totalAmount = totalAmount * 0.90;
            }

            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusMonths(monthsPaid);

            String passCode = "SP-PASS-" + (10000 + new Random().nextInt(90000));

            MonthlyPass pass = new MonthlyPass();
            pass.setUserId(userId);
            pass.setVehicleNumber(vehicleNumber);
            pass.setVehicleType(vehicleType);
            pass.setSlotId(slotId);
            pass.setStartDate(Date.valueOf(startDate));
            pass.setEndDate(Date.valueOf(endDate));
            pass.setMonthsPaid(monthsPaid);
            pass.setAmountPaid(totalAmount);
            pass.setPassCode(passCode);
            pass.setStatus("ACTIVE");

            boolean success = passDAO.createMonthlyPass(pass);

            if (success) {
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("message", "Monthly Pass successfully issued for Slot " + targetSlot.getSlotNumber() + "!");
                resp.put("passCode", passCode);
                resp.put("slotNumber", targetSlot.getSlotNumber());
                resp.put("vehicleNumber", vehicleNumber);
                resp.put("vehicleType", vehicleType);
                resp.put("startDate", startDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
                resp.put("endDate", endDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
                resp.put("amountPaid", totalAmount);
                resp.put("monthsPaid", monthsPaid);
                sendJsonResponse(exchange, 200, resp);
            } else {
                sendJsonResponse(exchange, 500, Map.of("success", false, "message", "Failed to process monthly pass subscription."));
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, Map.of("success", false, "message", "Error: " + e.getMessage()));
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
            sendJsonResponse(exchange, 400, Map.of("success", false, "message", "userId query parameter is required."));
            return;
        }

        List<MonthlyPass> list = passDAO.getPassesByUserId(userId);
        List<Map<String, Object>> resultList = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");

        for (MonthlyPass p : list) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("passId", p.getPassId());
            map.put("vehicleNumber", p.getVehicleNumber());
            map.put("vehicleType", p.getVehicleType());
            map.put("slotId", p.getSlotId());
            map.put("slotNumber", p.getSlotNumber());
            map.put("startDate", p.getStartDate() != null ? p.getStartDate().toLocalDate().format(dtf) : "");
            map.put("endDate", p.getEndDate() != null ? p.getEndDate().toLocalDate().format(dtf) : "");
            map.put("monthsPaid", p.getMonthsPaid());
            map.put("amountPaid", p.getAmountPaid());
            map.put("passCode", p.getPassCode());
            map.put("status", p.getStatus());
            resultList.add(map);
        }

        sendJsonResponse(exchange, 200, Map.of("success", true, "passes", resultList));
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
