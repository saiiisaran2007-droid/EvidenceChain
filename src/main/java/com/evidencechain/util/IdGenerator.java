package com.evidencechain.util;

/**
 * Utility for generating standardized sequential IDs for evidence items.
 */
public class IdGenerator {

    private static final String EVIDENCE_PREFIX = "EV-";

    private IdGenerator() {
        // Utility class
    }

    /**
     * Formats a sequential number into a standardized Evidence ID (e.g. EV-0001).
     *
     * @param sequenceNumber the sequence index (1-based)
     * @return formatted ID string (e.g. EV-0001)
     */
    public static String formatEvidenceId(int sequenceNumber) {
        return String.format("%s%04d", EVIDENCE_PREFIX, Math.max(1, sequenceNumber));
    }
}
