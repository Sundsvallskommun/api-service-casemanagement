package se.sundsvall.casemanagement.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casemanagement.TestUtil;

class CaseUtilTest {
    
    @Test
    void testGetSokigoFormattedOrganizationNumber() {
        String organizationNumber = TestUtil.generateRandomOrganizationNumber();
        String result = CaseUtil.getSokigoFormattedOrganizationNumber(organizationNumber);
        assertTrue(result.startsWith("16"));
        assertEquals(13, result.length());
        assertEquals("16" + organizationNumber, result);
    }
    
    @Test
    void testGetSokigoFormattedOrganizationNumberNoDifference() {
        String organizationNumber = TestUtil.generateRandomOrganizationNumber();
        organizationNumber = "10" + organizationNumber;
        String result = CaseUtil.getSokigoFormattedOrganizationNumber(organizationNumber);
        assertEquals(organizationNumber, result);
    }
    
    @Test
    void testGetSokigoFormattedOrganizationNumberWrongNumberOfDigits() {
        String organizationNumber = "123456-123";
        var problem = assertThrows(ThrowableProblem.class, () -> CaseUtil.getSokigoFormattedOrganizationNumber(organizationNumber));
        assertEquals(Status.BAD_REQUEST, problem.getStatus());
        assertEquals("organizationNumber must consist of 10 or 12 digits", problem.getDetail());
    }
    
    @Test
    void testParseBoolean() {
        assertTrue(CaseUtil.parseBoolean("true"));
        assertFalse(CaseUtil.parseBoolean("false"));
        assertTrue(CaseUtil.parseBoolean(true));
        assertFalse(CaseUtil.parseBoolean(false));
    }
    
    @Test
    void testGetSokigoFormattedPersonalNumberFromNonHyphenatedPersonalNumber() {
        var personalNumber = TestUtil.generateRandomPersonalNumber();
        var result = CaseUtil.getSokigoFormattedPersonalNumber(personalNumber);
        
        var expected = new StringBuilder(personalNumber).insert(8, "-").toString();
        
        assertNotEquals(personalNumber, result);
        assertEquals(expected, result);
    }
    
    @Test
    void testGetSokigoFormattedPersonalNumberFromProperlyFormattedPersonalNumber() {
        var personalNumber = new StringBuilder(TestUtil.generateRandomPersonalNumber()).insert(8, "-").toString();
        var result = CaseUtil.getSokigoFormattedPersonalNumber(personalNumber);
        assertEquals(personalNumber, result);
    }
    
    
    @Test
    void testGetSokigoFormattedPersonalNumberFromNull(){
        assertThrows(ThrowableProblem.class, () -> CaseUtil.getSokigoFormattedPersonalNumber(null));
    }
    @Test
    void testGetSokigoFormattedPersonalNumberFromWronglyFormattedPersonalNumber(){
        assertThrows(ThrowableProblem.class, () -> CaseUtil.getSokigoFormattedPersonalNumber("19000101"));
    }
    
    @Test
    void testParseBooleanWierdText() {
        assertFalse(CaseUtil.parseBoolean("wierd text"));
    }
    
    
    @Test
    void testParseBooleanNull() {
        assertFalse(CaseUtil.parseBoolean(null));
    }
    
    @Test
    void testParseInteger() {
        assertEquals(Integer.valueOf(100), CaseUtil.parseInteger("100"));
        assertEquals(Integer.valueOf(100), CaseUtil.parseInteger(100));
    }
    
    @Test
    void testParseIntegerNull() {
        assertNull(CaseUtil.parseInteger(null));
    }
    
    @Test
    void testParseIntegerWierdText() {
        assertThrows(NumberFormatException.class, () -> CaseUtil.parseInteger("wierd text"));
    }
    
    @Test
    void testParseDouble() {
        assertEquals(Double.valueOf(100.0), CaseUtil.parseDouble("100.0"));
        assertEquals(Double.valueOf(100.0), CaseUtil.parseDouble(100.0));
    }
    
    @Test
    void testParseDoubleNull() {
        assertNull(CaseUtil.parseDouble(null));
    }
    
    @Test
    void testParseDoubleWierdText() {
        assertThrows(NumberFormatException.class, () -> CaseUtil.parseDouble("wierd text"));
    }
    
    @Test
    void testParseLocalDateTime() {
        LocalDate localDate = LocalDate.now();
        assertEquals(localDate.atStartOfDay(), CaseUtil.parseLocalDateTime(localDate.toString()));
    }
    
    @Test
    void testParseLocalDateTimeNull() {
        assertNull(CaseUtil.parseLocalDateTime(null));
    }
    
    @Test
    void testParseLocalDateTimeWierdText() {
        assertThrows(DateTimeParseException.class, () -> CaseUtil.parseLocalDateTime("wierd text"));
    }
    
    @Test
    void testParseString() {
        assertEquals("test", CaseUtil.parseString("test"));
    }
    
    @Test
    void testParseStringNull() {
        assertNull(CaseUtil.parseString(null));
    }
    
    @Test
    void testParseStringEmpty() {
        assertEquals("", CaseUtil.parseString(""));
    }
    
    @Test
    void testParseStringBlank() {
        assertEquals(" ", CaseUtil.parseString(" "));
    }
}


