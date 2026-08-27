package com.smartpark.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DateTime formatting utility.
 */
public class DateTimeUtil {
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm:ss a");
    private static final DateTimeFormatter SHORT_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    public static String formatForDb(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DB_FORMATTER);
    }

    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DISPLAY_FORMATTER);
    }

    public static String formatShort(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(SHORT_FORMATTER);
    }

    public static LocalDateTime parseFromDb(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        return LocalDateTime.parse(str.replace("T", " "), DB_FORMATTER);
    }
}
