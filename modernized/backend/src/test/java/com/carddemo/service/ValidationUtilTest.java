package com.carddemo.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationUtilTest {

    @Test
    void isBlank_treatsNullEmptyWhitespaceAndStarAsBlank() {
        assertTrue(ValidationUtil.isBlank(null));
        assertTrue(ValidationUtil.isBlank(""));
        assertTrue(ValidationUtil.isBlank("   "));
        assertTrue(ValidationUtil.isBlank("*"));
        assertFalse(ValidationUtil.isBlank("USER0001"));
    }

    @Test
    void isValidDate_acceptsRealDatesAndRejectsInvalidOnes() {
        assertTrue(ValidationUtil.isValidDate("2024-02-29")); // leap day
        assertFalse(ValidationUtil.isValidDate("2023-02-29")); // not a leap year
        assertFalse(ValidationUtil.isValidDate("2024-13-01"));
        assertFalse(ValidationUtil.isValidDate("10/15/2024"));
        assertFalse(ValidationUtil.isValidDate(null));
    }

    @Test
    void isNumeric_acceptsDigitsOnly() {
        assertTrue(ValidationUtil.isNumeric("12345"));
        assertFalse(ValidationUtil.isNumeric("12a45"));
        assertFalse(ValidationUtil.isNumeric("12.5"));
        assertFalse(ValidationUtil.isNumeric(null));
    }

    @Test
    void isSignedDecimal_matchesCobolNumvalCShape() {
        assertTrue(ValidationUtil.isSignedDecimal("125.50"));
        assertTrue(ValidationUtil.isSignedDecimal("-125.50"));
        assertTrue(ValidationUtil.isSignedDecimal("+100"));
        assertFalse(ValidationUtil.isSignedDecimal("125.555")); // 3 decimals not allowed
        assertFalse(ValidationUtil.isSignedDecimal("abc"));
        assertFalse(ValidationUtil.isSignedDecimal(null));
    }

    @Test
    void parseDecimal_returnsExactValue() {
        assertEquals(new BigDecimal("125.50"), ValidationUtil.parseDecimal(" 125.50 "));
        assertThrows(NumberFormatException.class, () -> ValidationUtil.parseDecimal("x"));
    }

    @Test
    void isYesNo_acceptsYorNOnly() {
        assertTrue(ValidationUtil.isYesNo("Y"));
        assertTrue(ValidationUtil.isYesNo("n"));
        assertFalse(ValidationUtil.isYesNo("X"));
        assertFalse(ValidationUtil.isYesNo(null));
    }

    @Test
    void isValidState_recognizesUsStateCodes() {
        assertTrue(ValidationUtil.isValidState("WA"));
        assertTrue(ValidationUtil.isValidState("ny")); // case-insensitive
        assertTrue(ValidationUtil.isValidState("PR")); // territory included
        assertFalse(ValidationUtil.isValidState("XX"));
        assertFalse(ValidationUtil.isValidState(null));
    }

    @Test
    void isValidZip_acceptsZipAndZipPlus4() {
        assertTrue(ValidationUtil.isValidZip("98109"));
        assertTrue(ValidationUtil.isValidZip("98109-1234"));
        assertFalse(ValidationUtil.isValidZip("9810"));
        assertFalse(ValidationUtil.isValidZip("98109-123"));
        assertFalse(ValidationUtil.isValidZip(null));
    }

    @Test
    void isValidPhone_acceptsCommonFormats() {
        assertTrue(ValidationUtil.isValidPhone("2065551234"));
        assertTrue(ValidationUtil.isValidPhone("(206)555-1234"));
        assertTrue(ValidationUtil.isValidPhone("206-555-1234"));
        assertFalse(ValidationUtil.isValidPhone("555"));
        assertFalse(ValidationUtil.isValidPhone(null));
    }

    @Test
    void isAlpha_acceptsNamesAndRejectsDigits() {
        assertTrue(ValidationUtil.isAlpha("MARGARET GOLD"));
        assertTrue(ValidationUtil.isAlpha("O'Brien-Smith"));
        assertFalse(ValidationUtil.isAlpha("Name123"));
        assertFalse(ValidationUtil.isAlpha(null));
    }

    @Test
    void todayAndNow_returnFormattedStrings() {
        assertTrue(ValidationUtil.today().matches("\\d{4}-\\d{2}-\\d{2}"));
        assertTrue(ValidationUtil.now().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{6}"));
    }
}
