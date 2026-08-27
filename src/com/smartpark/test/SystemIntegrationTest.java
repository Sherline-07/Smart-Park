package com.smartpark.test;

import com.smartpark.dao.WorkerDAO;
import com.smartpark.model.BillingReceipt;
import com.smartpark.model.DashboardStats;
import com.smartpark.model.ParkingSlot;
import com.smartpark.model.Worker;
import com.smartpark.service.BillingService;
import com.smartpark.service.ParkingService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * SystemIntegrationTest verifies all core requirements and acceptance criteria:
 * - Worker authentication
 * - Slot management and status transitions
 * - Automated slot allocation
 * - Duplicate vehicle check
 * - Billing calculations and duration rounding
 * - Dynamic surge pricing when occupancy > 80%
 * - Real-time statistics aggregation
 */
public class SystemIntegrationTest {
    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println("   SMART PARK - SYSTEM INTEGRATION & COMPLIANCE TEST");
        System.out.println("========================================================");

        int passed = 0;
        int failed = 0;

        WorkerDAO workerDAO = new WorkerDAO();
        ParkingService parkingService = new ParkingService();
        BillingService billingService = new BillingService();

        // ----------------------------------------------------
        // Test 1: Worker Login
        // ----------------------------------------------------
        try {
            System.out.print("[TEST 1] Testing Worker Login Authentication... ");
            Worker admin = workerDAO.authenticate("admin", "admin123");
            Worker invalid = workerDAO.authenticate("admin", "wrongpassword");

            if (admin != null && "ADMIN".equalsIgnoreCase(admin.getRole()) && invalid == null) {
                System.out.println("PASSED (admin login OK, invalid credentials rejected)");
                passed++;
            } else {
                System.out.println("FAILED");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // ----------------------------------------------------
        // Test 2: Initial Slot Loading
        // ----------------------------------------------------
        try {
            System.out.print("[TEST 2] Verifying Real Parking Slots from Database... ");
            List<ParkingSlot> slots = parkingService.getAllSlots();
            if (slots.size() >= 30) {
                System.out.println("PASSED (" + slots.size() + " slots loaded)");
                passed++;
            } else {
                System.out.println("FAILED (Expected >= 30 slots, got " + slots.size() + ")");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // ----------------------------------------------------
        // Test 3: Vehicle Entry & Automatic Slot Allocation
        // ----------------------------------------------------
        String testPlate4W = "TN01AB1234";
        String testPlate2W = "TN02CD5678";
        try {
            System.out.print("[TEST 3] Testing Vehicle Entry (4W Car: " + testPlate4W + ")... ");
            Map<String, Object> entryResult = parkingService.processEntry(testPlate4W, "4W");
            String assignedSlot = (String) entryResult.get("slotNumber");

            if (assignedSlot != null && assignedSlot.startsWith("A-")) {
                System.out.println("PASSED (Assigned: " + assignedSlot + ")");
                passed++;
            } else {
                System.out.println("FAILED");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // ----------------------------------------------------
        // Test 4: Duplicate Vehicle Rejection
        // ----------------------------------------------------
        try {
            System.out.print("[TEST 4] Testing Duplicate Active Vehicle Rejection... ");
            try {
                parkingService.processEntry(testPlate4W, "4W");
                System.out.println("FAILED (Duplicate should have thrown exception)");
                failed++;
            } catch (IllegalArgumentException e) {
                System.out.println("PASSED (Correctly rejected: " + e.getMessage() + ")");
                passed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // ----------------------------------------------------
        // Test 5: 2W Vehicle Entry
        // ----------------------------------------------------
        try {
            System.out.print("[TEST 5] Testing Vehicle Entry (2W Bike: " + testPlate2W + ")... ");
            Map<String, Object> entryResult = parkingService.processEntry(testPlate2W, "2W");
            String assignedSlot = (String) entryResult.get("slotNumber");

            if (assignedSlot != null && assignedSlot.startsWith("B-")) {
                System.out.println("PASSED (Assigned: " + assignedSlot + ")");
                passed++;
            } else {
                System.out.println("FAILED");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // ----------------------------------------------------
        // Test 6: Duration Rounding & Billing Calculation
        // ----------------------------------------------------
        try {
            System.out.print("[TEST 6] Testing Billing & Duration Rounding (3.5 hours = 4 billable hours)... ");
            LocalDateTime entry = LocalDateTime.now().minusHours(3).minusMinutes(30);
            LocalDateTime exit = LocalDateTime.now();

            BillingReceipt receipt = billingService.calculateBill(999, "TEST999", "4W", "A-01", entry, exit, 50.0);

            // 4W base rate = ₹40. 4 billable hours * 40 = ₹160
            if (receipt.getBillableHours() == 4 && receipt.getHourlyRate() == 40.0 && receipt.getTotalAmount() == 160.0) {
                System.out.println("PASSED (Billable Hours: 4, Rate: ₹40/hr, Total: ₹160.00)");
                passed++;
            } else {
                System.out.println("FAILED (Got hours: " + receipt.getBillableHours() + ", Total: " + receipt.getTotalAmount() + ")");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // ----------------------------------------------------
        // Test 7: Dynamic Pricing Rule (> 80% Occupancy)
        // ----------------------------------------------------
        try {
            System.out.print("[TEST 7] Testing Dynamic Pricing Engine at 85% Occupancy... ");
            LocalDateTime entry = LocalDateTime.now().minusHours(2);
            LocalDateTime exit = LocalDateTime.now();

            // At 85% occupancy (>80%), 4W surge rate is ₹60/hr
            BillingReceipt surgeReceipt = billingService.calculateBill(998, "SURGE4W", "4W", "A-02", entry, exit, 85.0);

            if (surgeReceipt.isDynamicPricingApplied() && surgeReceipt.getHourlyRate() == 60.0 && surgeReceipt.getTotalAmount() == 120.0) {
                System.out.println("PASSED (Surge Rate Applied: ₹60/hr, 2 hrs = ₹120.00)");
                passed++;
            } else {
                System.out.println("FAILED");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // ----------------------------------------------------
        // Test 8: Vehicle Exit & Slot Release
        // ----------------------------------------------------
        try {
            System.out.print("[TEST 8] Processing Vehicle Exit & Slot Release for " + testPlate4W + "... ");
            BillingReceipt receipt = parkingService.processExit(testPlate4W);

            if (receipt != null && receipt.getSlotNumber() != null && receipt.getTotalAmount() > 0) {
                System.out.println("PASSED (Released: " + receipt.getSlotNumber() + ", Total: ₹" + receipt.getTotalAmount() + ")");
                passed++;
            } else {
                System.out.println("FAILED");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // ----------------------------------------------------
        // Test 9: Dashboard Real-time Stats Verification
        // ----------------------------------------------------
        try {
            System.out.print("[TEST 9] Verifying Live Dashboard Metrics from Database... ");
            DashboardStats stats = parkingService.getDashboardStats();

            if (stats.getTotalSlots() > 0 && stats.getAvailableSlots() + stats.getOccupiedSlots() + stats.getReservedSlots() == stats.getTotalSlots()) {
                System.out.println("PASSED (Total: " + stats.getTotalSlots() + ", Avail: " + stats.getAvailableSlots() + ", Occ: " + stats.getOccupiedSlots() + ", Occ%: " + stats.getOccupancyPercentage() + "%)");
                passed++;
            } else {
                System.out.println("FAILED");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // ----------------------------------------------------
        // Summary
        // ----------------------------------------------------
        System.out.println("========================================================");
        System.out.println("   TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("========================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
