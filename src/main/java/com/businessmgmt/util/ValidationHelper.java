package com.businessmgmt.util;

import com.businessmgmt.exception.ValidationException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Helper class with static validation methods.
 */
public class ValidationHelper {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{10}$");

    private ValidationHelper() {
        // Utility class — no instantiation
    }

    /**
     * Checks whether a phone number contains exactly 10 numeric digits.
     *
     * @param phone the phone string to check
     * @return true if exactly 10 digits
     */
    public static boolean isValidPhone(String phone) {
        if (isNullOrEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Checks whether a string is null or empty (after trimming).
     *
     * @param value the string to check
     * @return true if null or blank
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Validates that a string looks like a valid email address.
     *
     * @param email the string to check
     * @return true if the email is syntactically valid
     */
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Checks whether a numeric string represents a positive number.
     *
     * @param value the string to check
     * @return true if parseable and &gt; 0
     */
    public static boolean isPositiveNumber(String value) {
        if (isNullOrEmpty(value)) {
            return false;
        }
        try {
            double number = Double.parseDouble(value.trim());
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks whether a string represents a valid price (non-negative, up to 2 decimals).
     *
     * @param value the string to check
     * @return true if valid
     */
    public static boolean isValidPrice(String value) {
        if (isNullOrEmpty(value)) {
            return false;
        }
        try {
            BigDecimal price = new BigDecimal(value.trim());
            return price.compareTo(BigDecimal.ZERO) >= 0 && price.scale() <= 2;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates that a required field is not null or empty.
     * Throws a {@link ValidationException} if the field is blank.
     *
     * @param value     the field value
     * @param fieldName the human-readable field name (for the error message)
     * @throws ValidationException if validation fails
     */
    public static void validateRequired(String value, String fieldName) throws ValidationException {
        if (isNullOrEmpty(value)) {
            throw new ValidationException(fieldName + " is required and cannot be empty.");
        }
    }
}
