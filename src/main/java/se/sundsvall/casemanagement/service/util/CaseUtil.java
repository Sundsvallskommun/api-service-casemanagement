package se.sundsvall.casemanagement.service.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.zalando.problem.Problem;
import org.zalando.problem.Status;

public class CaseUtil {
    
    private CaseUtil() {
        throw new IllegalStateException("Utility class");
    }
    
    public static String getSokigoFormattedOrganizationNumber(String organizationNumber) {
        // The length is 1 more than the number of numbers because the text contains a hyphen
        if (organizationNumber.length() == 13) {
            return organizationNumber;
        } else if (organizationNumber.length() == 11) {
            return "16" + organizationNumber;
        } else {
            throw Problem.valueOf(Status.BAD_REQUEST, "organizationNumber must consist of 10 or 12 digits");
        }
    }
    
    public static String getSokigoFormattedPersonalNumber(String personalNumber) {
        
        if(personalNumber == null){
            throw Problem.valueOf(Status.BAD_REQUEST, "personalNumber must not be null");
        }
    
        // Validates that the personalNumber is correct example: 20220622-2396
        String properlyFormattedRegex = "^(19|20)\\d{2}(0[1-9]|1[012])(0[1-9]|1[0-9]|2[0-9]|3[0-1])-\\d{4}$";
        // Validates that the personalNumber is correct but missing - example: 202206222396
        String missingHypen = "^(19|20)\\d{2}(0[1-9]|1[012])(0[1-9]|1[0-9]|2[0-9]|3[0-1])\\d{4}$";
        
        if (personalNumber.matches(properlyFormattedRegex)) {
            return personalNumber;
        } else if (personalNumber.matches(missingHypen)) {
            return new StringBuilder(personalNumber).insert(8, "-").toString();
        }else {
            throw Problem.valueOf(Status.BAD_REQUEST, "personalNumber must be 12 digits");
        }
    }
    /**
     * Returns boolean, true or false.
     */
    public static boolean parseBoolean(Object value) {
        String stringValue = parseString(value);
        return Boolean.parseBoolean(stringValue);
    }
    
    /**
     * Returns Integer or null.
     */
    public static Integer parseInteger(Object value) {
        String stringValue = parseString(value);
        return (stringValue != null) ? Integer.parseInt(stringValue) : null;
    }
    
    /**
     * Returns LocalDateTime or null.
     * Parses a date without an offset, such as '2011-12-03'.
     */
    public static LocalDateTime parseLocalDateTime(Object value) {
        String stringValue = parseString(value);
        return (stringValue != null) ? LocalDate.parse(stringValue, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay() : null;
    }
    
    /**
     * Returns Double or null.
     */
    public static Double parseDouble(Object value) {
        String stringValue = parseString(value);
        return (stringValue != null) ? Double.parseDouble(stringValue) : null;
    }
    
    /**
     * Returns String or null.
     */
    public static String parseString(Object value) {
        return Objects.toString(value, null);
    }
    
    public static boolean notNullOrEmpty(List<?> list) {
        return (list != null && !list.isEmpty());
    }
}
