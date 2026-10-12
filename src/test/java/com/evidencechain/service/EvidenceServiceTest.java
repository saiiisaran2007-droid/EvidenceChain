package com.evidencechain.service;

import com.evidencechain.database.DatabaseManager;
import com.evidencechain.model.Evidence;
import com.evidencechain.repository.EvidenceRepository;
import com.evidencechain.util.FileHasher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EvidenceServiceTest {

    private static final String TEST_DB = "test_evidence_service.db";
    private EvidenceService evidenceService;
    private File tempTestFile;

    @BeforeEach
    void setUp() throws IOException {
        DatabaseManager.setDatabasePath(TEST_DB);
        DatabaseManager.initializeDatabase();

        EvidenceRepository repository = new EvidenceRepository();
        HashService hashService = new HashService();
        evidenceService = new EvidenceService(repository, hashService);

        // Create a temporary sample evidence file
        tempTestFile = File.createTempFile("evidence_test_", ".raw");
        try (FileWriter writer = new FileWriter(tempTestFile)) {
            writer.write("DIGITAL FORENSIC SAMPLE ARTIFACT: 2026-09-30-EVIDENCE-PAYLOAD");
        }
    }

    @AfterEach
    void tearDown() {
        if (tempTestFile != null && tempTestFile.exists()) {
            tempTestFile.delete();
        }
        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    void testRegisterEvidenceSuccess() throws Exception {
        Evidence registered = evidenceService.registerEvidence(
                "CASE-2026-99",
                "Disk Image (.img / .dd / .e01)",
                "Hard drive seized from suspect workstation",
                "Agent Vance",
                "Workstation DELL-7090",
                tempTestFile
        );

        assertNotNull(registered, "Registered evidence must not be null");
        assertEquals("EV-0001", registered.getEvidenceId(), "First evidence ID should be EV-0001");
        assertEquals("CASE-2026-99", registered.getCaseId());
        assertEquals("Agent Vance", registered.getCollector());

        // Validate real SHA-256 calculation
        String expectedHash = FileHasher.calculateSHA256(tempTestFile);
        assertNotNull(registered.getSha256Hash());
        assertEquals(expectedHash, registered.getSha256Hash(), "Recorded SHA-256 hash must match real file hash");
        assertEquals(64, registered.getSha256Hash().length(), "SHA-256 hash must be 64 hexadecimal characters");

        // Verify retrieval from SQLite database
        Optional<Evidence> fromDb = evidenceService.getEvidenceById("EV-0001");
        assertTrue(fromDb.isPresent(), "Evidence record must be present in SQLite");
        assertEquals("EV-0001", fromDb.get().getEvidenceId());
        assertEquals(expectedHash, fromDb.get().getSha256Hash());

        // Test second registration gets sequential ID EV-0002
        Evidence second = evidenceService.registerEvidence(
                "CASE-2026-99",
                "Memory Dump (.raw / .dmp)",
                "RAM acquisition artifact",
                "Agent Vance",
                "Workstation DELL-7090",
                tempTestFile
        );
        assertEquals("EV-0002", second.getEvidenceId(), "Second evidence ID should be EV-0002");

        List<Evidence> all = evidenceService.getAllEvidence();
        assertEquals(2, all.size(), "Should have 2 records in database");
    }

    @Test
    void testRegisterEvidenceMissingFileThrowsException() {
        File nonExistentFile = new File("C:\\non_existent_folder_xyz\\file.dd");

        assertThrows(IOException.class, () -> {
            evidenceService.registerEvidence(
                    "CASE-1",
                    "Log File",
                    "Notes",
                    "Collector",
                    "Device",
                    nonExistentFile
            );
        }, "Should throw IOException when file does not exist");
    }

    @Test
    void testRegisterEvidenceValidationErrors() {
        // Missing case ID
        assertThrows(IllegalArgumentException.class, () -> {
            evidenceService.registerEvidence("", "Log File", "Notes", "Collector", "Device", tempTestFile);
        });

        // Missing collector
        assertThrows(IllegalArgumentException.class, () -> {
            evidenceService.registerEvidence("CASE-1", "Log File", "Notes", "", "Device", tempTestFile);
        });

        // Missing device
        assertThrows(IllegalArgumentException.class, () -> {
            evidenceService.registerEvidence("CASE-1", "Log File", "Notes", "Collector", "   ", tempTestFile);
        });

        // Null file
        assertThrows(IllegalArgumentException.class, () -> {
            evidenceService.registerEvidence("CASE-1", "Log File", "Notes", "Collector", "Device", null);
        });
    }
}
