package com.smartpark.controller;

import com.smartpark.dao.ParkingSlotDAO;
import com.smartpark.dao.ReservationDAO;
import com.smartpark.model.ParkingSlot;
import com.smartpark.model.Reservation;
import com.smartpark.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * UserReservationController manages customer slot pre-bookings.
 * Endpoints:
 *   POST /api/user/reservations/create
 *   GET  /api/user/reservations/list
 *   POST /api/user/reservations/cancel
 */
public class UserReservationController implements HttpHandler {
    private final ReservationDAO reservationDAO = new ReservationDAO();
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
            } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/create")) {
                handleCreate(exchange);
            } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/cancel")) {
                handleCancel(exchange);
            } else {
                sendJsonResponse(exchange, 404, Map.of("success", false, "message", "Endpoint Not Found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, Map.of("success", false, "message", "Server Error: " + e.getMessage()));
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
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
        String slotIdStr = data.get("slotId");
        String scheduledEntryStr = data.get("scheduledEntry");
        String durationHoursStr = data.get("durationHours");

        if (userIdStr == null || vehicleNumber == null || vehicleType == null) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "message", "User ID, vehicle number, and vehicle type are required."));
            return;
        }

        int userId = Integer.parseInt(userIdStr);
        vehicleNumber = vehicleNumber.trim().toUpperCase();
        vehicleType = vehicleType.trim().toUpperCase();
        int durationHours = durationHoursStr != null ? Integer.parseInt(durationHoursStr) : 2;

        int slotId = -1;
        ParkingSlot targetSlot = null;

        try {
            if (slotIdStr != null && !slotIdStr.trim().isEmpty()) {
                slotId = Integer.parseInt(slotIdStr);
                targetSlot = slotDAO.getSlotById(slotId);
                if (targetSlot == null || !"AVAILABLE".equalsIgnoreCase(targetSlot.getStatus())) {
                    sendJsonResponse(exchange, 400, Map.of("success", false, "message", "Selected slot is no longer available. Please choose another slot."));
                    return;
                }
            } else {
                // Auto-allocate lowest available slot for vehicle category
                targetSlot = slotDAO.getAvailableSlot(vehicleType);
                if (targetSlot == null) {
                    sendJsonResponse(exchange, 400, Map.of("success", false, "message", "No available parking slots for " + vehicleType + "."));
                    return;
                }
                slotId = targetSlot.getSlotId();
            }

            // Calculate estimated fee
            double hourlyRate = vehicleType.equalsIgnoreCase("4W") ? 40.0 : 20.0;
            double estimatedFee = hourlyRate * durationHours;

            Timestamp scheduledTimestamp;
            if (scheduledEntryStr != null && !scheduledEntryStr.trim().isEmpty()) {
                try {
                    scheduledTimestamp = Timestamp.valueOf(LocalDateTime.parse(scheduledEntryStr));
                } catch (Exception e) {
                    scheduledTimestamp = new Timestamp(System.currentTimeMillis());
                }
            } else {
                scheduledTimestamp = new Timestamp(System.currentTimeMillis());
            }

            // Generate unique cyberpunk pass code (e.g. SP-RES-8492)
            String passCode = "SP-RES-" + (1000 + new Random().nextInt(9000));

            Reservation res = new Reservation();
            res.setUserId(userId);
            res.setVehicleNumber(vehicleNumber);
            res.setVehicleType(vehicleType);
            res.setSlotId(slotId);
            res.setScheduledEntry(scheduledTimestamp);
            res.setDurationHours(durationHours);
            res.setEstimatedFee(estimatedFee);
            res.setPassCode(passCode);
            res.setStatus("CONFIRMED");

            boolean success = reservationDAO.createReservation(res);

            if (success) {
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("message", "Slot " + targetSlot.getSlotNumber() + " successfully pre-booked!");
                resp.put("passCode", passCode);
                resp.put("slotNumber", targetSlot.getSlotNumber());
                resp.put("vehicleNumber", vehicleNumber);
                resp.put("vehicleType", vehicleType);
                resp.put("estimatedFee", estimatedFee);
                resp.put("durationHours", durationHours);
                resp.put("reservationId", res.getReservationId());
                sendJsonResponse(exchange, 200, resp);
            } else {
                sendJsonResponse(exchange, 500, Map.of("success", false, "message", "Failed to process pre-booking reservation."));
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

        List<Reservation> list = reservationDAO.getReservationsByUserId(userId);
        List<Map<String, Object>> resultList = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

        for (Reservation r : list) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("reservationId", r.getReservationId());
            map.put("vehicleNumber", r.getVehicleNumber());
            map.put("vehicleType", r.getVehicleType());
            map.put("slotId", r.getSlotId());
            map.put("slotNumber", r.getSlotNumber());
            map.put("scheduledEntry", r.getScheduledEntry() != null ? r.getScheduledEntry().toLocalDateTime().format(dtf) : "");
            map.put("durationHours", r.getDurationHours());
            map.put("estimatedFee", r.getEstimatedFee());
            map.put("passCode", r.getPassCode());
            map.put("status", r.getStatus());
            resultList.add(map);
        }

        sendJsonResponse(exchange, 200, Map.of("success", true, "reservations", resultList));
    }

    private void handleCancel(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            body.append(line);
        }

        Map<String, String> data = JsonUtil.parseObject(body.toString());
        String resIdStr = data.get("reservationId");
        String userIdStr = data.get("userId");

        if (resIdStr == null || userIdStr == null) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "message", "reservationId and userId are required."));
            return;
        }

        boolean success = reservationDAO.cancelReservation(Integer.parseInt(resIdStr), Integer.parseInt(userIdStr));
        if (success) {
            sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Reservation cancelled and slot released back to available."));
        } else {
            sendJsonResponse(exchange, 400, Map.of("success", false, "message", "Unable to cancel reservation. It may have already been checked in or cancelled."));
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
