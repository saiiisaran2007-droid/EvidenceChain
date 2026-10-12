package com.evidencechain.service;

import com.evidencechain.database.DatabaseManager;
import com.evidencechain.model.Evidence;
import com.evidencechain.model.IntegrityVerificationResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class IntegrityVerificationTest {

    private static final String TEST_DB = "test_integrity_service.db";
    private EvidenceService evidenceService;
    private File tempFile;
    private String originalContent = "CRITICAL FORENSIC DISK IMAGE ARTIFACT PAYLOAD";

    @BeforeEach
    void setUp() throws IOException {
        DatabaseManager.setDatabasePath(TEST_DB);
        DatabaseManager.initializeDatabase();
        evidenceService = new EvidenceService();

        tempFile = File.createTempFile("evidence_integrity_", ".raw");
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write(originalContent);
        }
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    void testIntegrityVerificationPassAndTamperDetection() throws Exception {
        // 1. Register file
        Evidence evidence = evidenceService.registerEvidence(
                "CASE-AUDIT-01",
                "Disk Image (.img / .dd / .e01)",
                "Raw disk capture",
                "Agent Miller",
                "Workstation Dell 9020",
                tempFile
        );

        // 2. Initial verification must PASS (INTEGRITY VERIFIED)
        IntegrityVerificationResult initialResult = evidenceService.verifyEvidenceIntegrity(evidence);
        assertTrue(initialResult.isVerified(), "Initial file state must verify successfully");
        assertEquals(IntegrityVerificationResult.Status.INTEGRITY_VERIFIED, initialResult.getStatus());
        assertEquals(evidence.getSha256Hash(), initialResult.getCalculatedHash());
        assertTrue(initialResult.getMessage().contains("INTEGRITY VERIFIED"));

        // 3. Tamper with file on disk by appending corrupted byte
        try (FileOutputStream fos = new FileOutputStream(tempFile, true)) {
            fos.write("\nMALICIOUS_ALTERATION_INJECTED".getBytes());
        }

        // 4. Verification must now DETECT TAMPERING
        IntegrityVerificationResult tamperedResult = evidenceService.verifyEvidenceIntegrity(evidence);
        assertFalse(tamperedResult.isVerified(), "Modified file must fail integrity verification");
        assertEquals(IntegrityVerificationResult.Status.TAMPER_DETECTED, tamperedResult.getStatus());
        assertNotEquals(evidence.getSha256Hash(), tamperedResult.getCalculatedHash());
        assertTrue(tamperedResult.getMessage().contains("TAMPER DETECTED"));

        // 5. Restore original content
        Files.write(tempFile.toPath(), originalContent.getBytes());

        // 6. Verification must now PASS again
        IntegrityVerificationResult restoredResult = evidenceService.verifyEvidenceIntegrity(evidence);
        assertTrue(restoredResult.isVerified(), "Restored file must pass integrity verification again");
        assertEquals(IntegrityVerificationResult.Status.INTEGRITY_VERIFIED, restoredResult.getStatus());

        // 7. Delete file on disk
        tempFile.delete();

        // 8. Verification must report FILE_NOT_FOUND (Tamper / Missing)
        IntegrityVerificationResult deletedResult = evidenceService.verifyEvidenceIntegrity(evidence);
        assertFalse(deletedResult.isVerified());
        assertEquals(IntegrityVerificationResult.Status.FILE_NOT_FOUND, deletedResult.getStatus());
        assertTrue(deletedResult.getMessage().contains("missing or inaccessible"));
    }
}
