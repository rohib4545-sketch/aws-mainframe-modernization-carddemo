package com.carddemo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Set;

/** Port of the shared date/field edit routines (CSUTLDTC/CSUTLDWY). */
public final class ValidationUtil {
    private ValidationUtil() {}

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    public static boolean isBlank(String s) { return s == null || s.isBlank() || s.equals("*"); }

    public static boolean isValidDate(String s) {
        if (s == null || !s.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        try { LocalDate.parse(s, FMT);
        return true;
        }
        catch (Exception e) { return false; }
    }

    public static boolean isNumeric(String s) {
        return s != null && s.matches("\\d+");
    }

    /** Signed decimal like COBOL NUMVAL-C: optional sign, digits, optional '.', decimals. */
    public static boolean isSignedDecimal(String s) {
        return s != null && s.matches("[+-]?\\d{1,9}(\\.\\d{1,2})?");
    }

    public static BigDecimal parseDecimal(String s) {
        return new BigDecimal(s.trim());
    }

    public static boolean isYesNo(String s) {
        return s != null && (s.equalsIgnoreCase("Y") || s.equalsIgnoreCase("N"));
    }

    private static final Set<String> US_STATES = Set.of(
        "AL","AK","AZ","AR","CA","CO","CT","DE","DC","FL","GA","HI","ID","IL","IN",
        "IA","KS","KY","LA","ME","MD","MA","MI","MN","MS","MO","MT","NE","NV","NH",
        "NJ","NM","NY","NC","ND","OH","OK","OR","PA","RI","SC","SD","TN","TX","UT",
        "VT","VA","WA","WV","WI","WY","AS","GU","MP","PR","VI");

    public static boolean isValidState(String s) {
        return s != null && US_STATES.contains(s.toUpperCase());
    }

    /** Zip: 5 digits, or ZIP+4 'NNNNN-NNNN'. */
    public static boolean isValidZip(String s) {
        return s != null && s.matches("\\d{5}(-\\d{4})?");
    }

    /** Phone like '(NNN)NNN-NNNN' or 10-15 digits, or blank-allowed. */
    public static boolean isValidPhone(String s) {
        return s != null && s.matches("[(]?\\d{3}[)]?[-.\\s]?\\d{3}[-.\\s]?\\d{4}(\\s*x\\d{1,5})?");
    }

    public static boolean isAlpha(String s) {
        return s != null && s.matches("[A-Za-z][A-Za-z .\\-']*");
    }

    public static String now() {
        return java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
    }

    public static String today() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
