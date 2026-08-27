package com.smartpark.service;

import com.smartpark.model.BillingReceipt;
import com.smartpark.util.DBConfig;
import com.smartpark.util.DateTimeUtil;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * BillingService manages pricing logic, duration calculation, and dynamic pricing rules.
 */
public class BillingService {

    /**
     * Calculates parking bill for a vehicle based on entry and exit timestamps,
     * vehicle category, and current occupancy percentage.
     */
    public BillingReceipt calculateBill(int billId, String vehicleNumber, String vehicleType,
                                         String slotNumber, LocalDateTime entryTime,
                                         LocalDateTime exitTime, double currentOccupancyPercentage) {
        if (exitTime == null) {
            exitTime = LocalDateTime.now();
        }
        if (entryTime == null) {
            entryTime = exitTime.minusHours(1);
        }

        // Calculate duration in minutes
        Duration duration = Duration.between(entryTime, exitTime);
        long minutes = duration.toMinutes();
        if (minutes < 1) {
            minutes = 1; // Minimum 1 minute recorded
        }

        // Rounding Rule:
        // - Up to 1 hour (<= 60 mins) -> 1 billable hour
        // - Any additional fraction of an hour -> round up to next full hour
        int billableHours;
        if (minutes <= 60) {
            billableHours = 1;
        } else {
            billableHours = (int) Math.ceil(minutes / 60.0);
        }

        // Check Dynamic Pricing condition (> 80% occupancy threshold)
        double threshold = DBConfig.getOccupancyThreshold();
        boolean isDynamic = currentOccupancyPercentage > threshold;

        double hourlyRate;
        String note;
        boolean is4W = "4W".equalsIgnoreCase(vehicleType);

        if (isDynamic) {
            hourlyRate = is4W ? DBConfig.get4WSurgeRate() : DBConfig.get2WSurgeRate();
            note = String.format("Dynamic Surge Applied (Occupancy %.1f%% > %.0f%% threshold)", 
                                 currentOccupancyPercentage, threshold);
        } else {
            hourlyRate = is4W ? DBConfig.get4WBaseRate() : DBConfig.get2WBaseRate();
            note = "Standard Parking Rate";
        }

        double totalAmount = billableHours * hourlyRate;

        // Build human-friendly duration text
        long hoursPart = minutes / 60;
        long minsPart = minutes % 60;
        String durationFormatted;
        if (hoursPart == 0) {
            durationFormatted = minsPart + " min" + (minsPart == 1 ? "" : "s");
        } else if (minsPart == 0) {
            durationFormatted = hoursPart + " hour" + (hoursPart == 1 ? "" : "s");
        } else {
            durationFormatted = hoursPart + " hr " + minsPart + " min";
        }

        BillingReceipt receipt = new BillingReceipt();
        receipt.setBillId(billId);
        receipt.setVehicleNumber(vehicleNumber.toUpperCase());
        receipt.setVehicleType(vehicleType);
        receipt.setSlotNumber(slotNumber);
        receipt.setEntryTime(DateTimeUtil.formatForDisplay(entryTime));
        receipt.setExitTime(DateTimeUtil.formatForDisplay(exitTime));
        receipt.setDurationMinutes((int) minutes);
        receipt.setDurationFormatted(durationFormatted);
        receipt.setBillableHours(billableHours);
        receipt.setHourlyRate(hourlyRate);
        receipt.setDynamicPricingApplied(isDynamic);
        receipt.setDynamicPricingNote(note);
        receipt.setTotalAmount(totalAmount);

        return receipt;
    }
}
