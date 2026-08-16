/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cornucopiabankqueuesystem;
import java.awt.Component;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author Kijetsu
 */
public class ValidationUtils {
     /** Letters, spaces, periods, apostrophes and hyphens - e.g. "Juan Dela Cruz". */
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-zÑñ][A-Za-zÑñ.'-]*(?:\\s[A-Za-zÑñ.'-]+)*$");
 
    /** Bank account numbers: digits only, 6-20 characters long. */
    private static final Pattern ACCOUNT_NUMBER_PATTERN =
            Pattern.compile("^\\d{6,20}$");
 
    /** Bill / reference numbers: letters, digits and hyphens, 4-30 characters. */
    private static final Pattern REFERENCE_PATTERN =
            Pattern.compile("^[A-Za-z0-9-]{4,30}$");
 
    /** Monetary amount: up to 12 whole digits and an optional 2-decimal part. */
    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("^\\d{1,12}(\\.\\d{1,2})?$");
 
    /** Whole-peso amount only (no decimals) - used for cash-dispensing withdrawals. */
    private static final Pattern WHOLE_AMOUNT_PATTERN =
            Pattern.compile("^\\d{1,12}$");
 
    private ValidationUtils() {
        // utility class - no instances
    }
 
    // ---------------------------------------------------------------
    // Generic field checks
    // ---------------------------------------------------------------
 
    public static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
 
    public static boolean isValidName(String text) {
        return !isBlank(text) && NAME_PATTERN.matcher(text.trim()).matches();
    }
 
    public static boolean isValidAccountNumber(String text) {
        return !isBlank(text) && ACCOUNT_NUMBER_PATTERN.matcher(text.trim()).matches();
    }
 
    public static boolean isValidReferenceNumber(String text) {
        return !isBlank(text) && REFERENCE_PATTERN.matcher(text.trim()).matches();
    }
 
    public static boolean isValidAmountFormat(String text) {
        return !isBlank(text) && AMOUNT_PATTERN.matcher(text.trim()).matches();
    }
 
    public static boolean isValidWholeAmountFormat(String text) {
        return !isBlank(text) && WHOLE_AMOUNT_PATTERN.matcher(text.trim()).matches();
    }
 
    /**
     * Parses a monetary string already known to match {@link #isValidAmountFormat}.
     * Returns {@code null} if the text is not a valid, positive amount.
     */
    public static Double parseAmount(String text) {
        if (!isValidAmountFormat(text)) {
            return null;
        }
        try {
            double value = Double.parseDouble(text.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
 
    /**
     * A combo box selection is only considered valid if something is selected
     * and the text isn't blank/a spacer placeholder (some of these forms use
     * " " entries purely as visual separators between real choices).
     */
    public static boolean isValidComboSelection(Object selectedItem) {
        return selectedItem != null && !selectedItem.toString().trim().isEmpty();
    }
 
    // ---------------------------------------------------------------
    // Dialog helpers
    // ---------------------------------------------------------------
 
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Invalid Input", JOptionPane.ERROR_MESSAGE);
    }
 
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
 
    /** Generates a short random queue/reference ticket number, e.g. "DP-4821". */
    public static String generateTicketNumber(String prefix) {
        int number = 1000 + (int) (Math.random() * 9000);
        return prefix + "-" + number;
    }
    
}
