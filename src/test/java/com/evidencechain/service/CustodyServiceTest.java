package com.evidencechain.service;

import com.evidencechain.database.DatabaseManager;
import com.evidencechain.model.CustodyEvent;
import com.evidencechain.model.Evidence;
import com.evidencechain.model.LedgerBlock;
import com.evidencechain.model.LedgerVerificationResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustodyServiceTest {

    private static final String TEST_DB = "test_custody_service.db";
    private EvidenceService evidenceService;
    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        DatabaseManager.setDatabasePath(TEST_DB);
        DatabaseManager.initializeDatabase();
        evidenceService = new EvidenceService();

        tempFile = File.createTempFile("evidence_test_", ".dat");
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write("TEST FORENSIC PAYLOAD 2026");
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
    void testCustodyLifecycleAndLedgerSynchronization() throws Exception {
        // 1. Register Evidence
        Evidence evidence = evidenceService.registerEvidence(
                "CASE-101",
                "Disk Image (.img / .dd / .e01)",
                "Seized Hard Disk",
                "Investigator Alice",
                "Laptop HP EliteBook",
                tempFile
        );

        String evId = evidence.getEvidenceId();

        // 2. Initial custody event created upon registration
        List<CustodyEvent> initialCustody = evidenceService.getCustodyEventsForEvidence(evId);
        assertEquals(1, initialCustody.size(), "Registration must create initial REGISTERED custody event");
        assertEquals("REGISTERED", initialCustody.get(0).getAction());
        assertEquals("Investigator Alice", initialCustody.get(0).getActorFrom());

        // 3. Add TRANSFERRED custody event
        CustodyEvent transferEvent = evidenceService.addCustodyEvent(
                evId,
                "TRANSFERRED",
                "Investigator Alice",
                "Analyst Bob (Digital Forensics Lab)",
                "Handed over for bitstream analysis"
        );
        assertNotNull(transferEvent);
        assertEquals("TRANSFERRED", transferEvent.getAction());

        // 4. Add ANALYSIS STARTED custody event
        evidenceService.addCustodyEvent(
                evId,
                "ANALYSIS STARTED",
                "Analyst Bob",
                "Forensics Workstation-03",
                "Mounted image read-only in Autopsy"
        );

        // 5. Add RETURNED custody event
        evidenceService.addCustodyEvent(
                evId,
                "RETURNED",
                "Analyst Bob",
                "Evidence Vault Room 204",
                "Analysis complete, returned to cold storage"
        );

        // 6. Verify chronological custody history
        List<CustodyEvent> events = evidenceService.getCustodyEventsForEvidence(evId);
        assertEquals(4, events.size(), "Must contain 4 custody events");
        assertEquals("REGISTERED", events.get(0).getAction());
        assertEquals("TRANSFERRED", events.get(1).getAction());
        assertEquals("ANALYSIS STARTED", events.get(2).getAction());
        assertEquals("RETURNED", events.get(3).getAction());

        // 7. Verify that every custody event created a ledger block
        List<LedgerBlock> blocks = evidenceService.getLedgerService().getAllBlocks();
        // 1 Genesis + 1 Registration + 3 Custody transfers = 5 blocks
        assertEquals(5, blocks.size(), "Ledger must contain 5 blocks total");

        assertEquals("GENESIS", blocks.get(0).getAction());
        assertEquals("REGISTERED", blocks.get(1).getAction());
        assertEquals("TRANSFERRED", blocks.get(2).getAction());
        assertEquals("ANALYSIS STARTED", blocks.get(3).getAction());
        assertEquals("RETURNED", blocks.get(4).getAction());

        // 8. Verify ledger integrity
        LedgerVerificationResult verification = evidenceService.getLedgerService().verifyLedger();
        assertTrue(verification.isValid(), "Ledger should remain valid across all custody transitions");
        assertEquals(5, verification.getTotalBlocks());
    }
}
