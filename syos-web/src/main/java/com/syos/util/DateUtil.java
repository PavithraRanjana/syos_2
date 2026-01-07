package com.syos.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date operations.
 */
public class DateUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private DateUtil() {
        // Prevent instantiation
    }

    /**
     * Formats a LocalDate to string.
     */
    public static String format(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    /**
     * Formats a LocalDateTime to string.
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    /**
     * Formats a date for display.
     */
    public static String formatForDisplay(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE_FORMATTER) : "-";
    }

    /**
     * Formats a datetime for display.
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_DATETIME_FORMATTER) : "-";
    }

    /**
     * Parses a date string.
     */
    public static LocalDate parse(String dateStr) {
        return dateStr != null && !dateStr.isEmpty() ?
            LocalDate.parse(dateStr, DATE_FORMATTER) : null;
    }

    /**
     * Parses a datetime string.
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr != null && !dateTimeStr.isEmpty() ?
            LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER) : null;
    }

    /**
     * Calculates days between two dates.
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculates days until a future date from today.
     */
    public static long daysUntil(LocalDate futureDate) {
        return ChronoUnit.DAYS.between(LocalDate.now(), futureDate);
    }

    /**
     * Calculates days since a past date from today.
     */
    public static long daysSince(LocalDate pastDate) {
        return ChronoUnit.DAYS.between(pastDate, LocalDate.now());
    }

    /**
     * Checks if a date is in the past (before today).
     */
    public static boolean isPast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Checks if a date is in the future (after today).
     */
    public static boolean isFuture(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Checks if a date is today.
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }

    /**
     * Checks if a date is within the given number of days from today.
     */
    public static boolean isWithinDays(LocalDate date, int days) {
        if (date == null) return false;
        LocalDate threshold = LocalDate.now().plusDays(days);
        return !date.isAfter(threshold);
    }

    /**
     * Gets the start of the current day.
     */
    public static LocalDateTime startOfDay() {
        return LocalDate.now().atStartOfDay();
    }

    /**
     * Gets the end of the current day.
     */
    public static LocalDateTime endOfDay() {
        return LocalDate.now().atTime(23, 59, 59);
    }

    /**
     * Gets the start of a specific date.
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * Gets the end of a specific date.
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(23, 59, 59);
    }
}
