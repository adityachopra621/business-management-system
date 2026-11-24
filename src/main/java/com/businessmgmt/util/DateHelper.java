package com.businessmgmt.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Helper class for date formatting and parsing.
 */
public class DateHelper {

    /** Standard date format used throughout the application. */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /** Standard date-time format used throughout the application. */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** Display-friendly date format. */
    public static final String DISPLAY_DATE_FORMAT = "MMM dd, yyyy";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern(DISPLAY_DATE_FORMAT);

    private DateHelper() {
        // Utility class — no instantiation
    }

    /**
     * Formats a LocalDate to the standard date string (yyyy-MM-dd).
     *
     * @param date the date to format
     * @return the formatted string, or empty string if null
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formats a LocalDateTime to the standard date-time string.
     *
     * @param dateTime the date-time to format
     * @return the formatted string, or empty string if null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Formats a LocalDate for display (e.g. "Nov 18, 2026").
     *
     * @param date the date to format
     * @return the display string, or empty string if null
     */
    public static String formatDisplayDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DISPLAY_FORMATTER);
    }

    /**
     * Parses a date string (yyyy-MM-dd) into a LocalDate.
     *
     * @param dateString the string to parse
     * @return the parsed LocalDate, or null if invalid
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a date-time string into a LocalDateTime.
     *
     * @param dateTimeString the string to parse
     * @return the parsed LocalDateTime, or null if invalid
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString.trim(), DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Returns today's date.
     *
     * @return the current LocalDate
     */
    public static LocalDate today() {
        return LocalDate.now();
    }
}
