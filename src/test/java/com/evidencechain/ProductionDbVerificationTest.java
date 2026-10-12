package com.evidencechain;

import com.evidencechain.database.DatabaseManager;
import com.evidencechain.model.Evidence;
import com.evidencechain.service.EvidenceService;
import com.evidencechain.util.FileHasher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductionDbVerificationTest {

    private static final String TEST_DB = "test_production_verification.db";

    @BeforeEach
    void setUp() {
        DatabaseManager.setDatabasePath(TEST_DB);
        DatabaseManager.initializeDatabase();
    }

    @AfterEach
    void tearDown() {
        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    void testRealFileRegistrationInSqlite() throws Exception {
        EvidenceService service = new EvidenceService();

        // Create a real sample evidence file on disk
        File sampleDir = new File("sample_evidence");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        File testArtifact = new File(sampleDir, "disk_capture_sample.dd");
        try (FileWriter fw = new FileWriter(testArtifact)) {
            fw.write("=== FORENSIC EVIDENCE ARTIFACT #2026-A1 ===\n");
            fw.write("Acquisition Date: 2026-09-30\n");
            fw.write("Hardware: Western Digital 1TB HDD Serial #WD-8472910\n");
            fw.write("Sector integrity: 100% verified\n");
        }

        // Calculate expected SHA-256 using FileHasher / MessageDigest directly
        String expectedSha256 = FileHasher.calculateSHA256(testArtifact);

        // Register through EvidenceService
        Evidence registered = service.registerEvidence(
                "CASE-2026-FORENSICS-01",
                "Disk Image (.img / .dd / .e01)",
                "Physical Bitstream image of seized Western Digital HDD",
                "Special Agent Alex Carter",
                "Seized Server Rack-04 (IP: 192.168.1.105)",
                testArtifact
        );

        assertNotNull(registered);
        assertTrue(registered.getEvidenceId().startsWith("EV-"));
        assertEquals(expectedSha256, registered.getSha256Hash());
        assertEquals("CASE-2026-FORENSICS-01", registered.getCaseId());

        // Verify that it is persistently stored and queryable in SQLite
        List<Evidence> allEvidence = service.getAllEvidence();
        assertFalse(allEvidence.isEmpty());

        boolean found = allEvidence.stream().anyMatch(e -> e.getEvidenceId().equals(registered.getEvidenceId())
                && e.getSha256Hash().equals(expectedSha256));
        assertTrue(found, "The registered evidence with exact SHA-256 must be present in SQLite");

        System.out.println(">>> VERIFIED: Evidence successfully saved in SQLite with ID: "
                + registered.getEvidenceId() + " and SHA-256: " + registered.getSha256Hash());
    }
}
