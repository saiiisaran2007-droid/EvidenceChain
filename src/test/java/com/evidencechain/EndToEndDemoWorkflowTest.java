package com.evidencechain;

import com.evidencechain.database.DatabaseManager;
import com.evidencechain.model.CustodyEvent;
import com.evidencechain.model.Evidence;
import com.evidencechain.model.IntegrityVerificationResult;
import com.evidencechain.model.LedgerBlock;
import com.evidencechain.model.LedgerVerificationResult;
import com.evidencechain.service.EvidenceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test validating the exact 13-step demonstration script:
 * 1. Open Dashboard (counts and status)
 * 2. Register a real test file
 * 3. Verify generated SHA-256
 * 4. Verify Evidence ID
 * 5. Open Evidence Details
 * 6. Add "TRANSFERRED" custody event
 * 7. Verify custody history
 * 8. Open Ledger
 * 9. Verify generated linked blocks
 * 10. Click Verify Evidence
 * 11. Verify "INTEGRITY VERIFIED"
 * 12. Click Verify Ledger
 * 13. Verify "LEDGER VALID"
 * + Bonus: Tamper file and verify "TAMPER DETECTED"
 */
class EndToEndDemoWorkflowTest {

    private static final String DEMO_DB = "test_demo_workflow.db";
    private EvidenceService evidenceService;
    private File demoFile;
    private String demoContent = "EVIDENCE FILE BITSTREAM ARTIFACT: SUSPECT DRIVE CLONE";

    @BeforeEach
    void setUp() throws IOException {
        DatabaseManager.setDatabasePath(DEMO_DB);
        DatabaseManager.initializeDatabase();
        evidenceService = new EvidenceService();

        demoFile = File.createTempFile("demo_seized_drive_", ".dd");
        try (FileWriter fw = new FileWriter(demoFile)) {
            fw.write(demoContent);
        }
    }

    @AfterEach
    void tearDown() {
        if (demoFile != null && demoFile.exists()) {
            demoFile.delete();
        }
        File db = new File(DEMO_DB);
        if (db.exists()) {
            db.delete();
        }
    }

    @Test
    void testExactDemoFlow() throws Exception {
        // Step 1: Open Dashboard — check initial state
        int initialEvidenceCount = evidenceService.getTotalEvidenceCount();
        assertEquals(0, initialEvidenceCount);
        LedgerVerificationResult initialLedger = evidenceService.getLedgerService().verifyLedger();
        assertTrue(initialLedger.isValid(), "Genesis block should make ledger valid");

        // Step 2: Register a real test file
        Evidence registered = evidenceService.registerEvidence(
                "CASE-DEMO-2026",
                "Disk Image (.img / .dd / .e01)",
                "Physical bitstream image from suspect workstation",
                "Lead Forensics Examiner Sara",
                "Workstation Dell OptiPlex SN#99281",
                demoFile
        );

        // Step 3 & 4: Show generated SHA-256 and Evidence ID
        assertNotNull(registered);
        assertNotNull(registered.getEvidenceId());
        assertTrue(registered.getEvidenceId().startsWith("EV-"));
        assertNotNull(registered.getSha256Hash());
        assertEquals(64, registered.getSha256Hash().length());
        System.out.println("Demo Step 3 & 4: Evidence ID = " + registered.getEvidenceId() + ", SHA-256 = " + registered.getSha256Hash());

        // Step 5: Open Evidence Details — inspect metadata and file path
        Evidence retrieved = evidenceService.getEvidenceById(registered.getEvidenceId()).orElseThrow();
        assertEquals(registered.getEvidenceId(), retrieved.getEvidenceId());
        assertEquals(demoFile.getAbsolutePath(), retrieved.getFilePath());

        // Step 6: Add "TRANSFERRED" custody event
        CustodyEvent transfer = evidenceService.addCustodyEvent(
                registered.getEvidenceId(),
                "TRANSFERRED",
                "Lead Forensics Examiner Sara",
                "Senior Analyst Dave (Cyber Lab)",
                "Relinquished for bitstream hash verification and metadata analysis"
        );
        assertNotNull(transfer);
        assertEquals("TRANSFERRED", transfer.getAction());

        // Step 7: Show custody history
        List<CustodyEvent> history = evidenceService.getCustodyEventsForEvidence(registered.getEvidenceId());
        assertEquals(2, history.size(), "Should have REGISTERED + TRANSFERRED events");
        assertEquals("REGISTERED", history.get(0).getAction());
        assertEquals("TRANSFERRED", history.get(1).getAction());

        // Step 8 & 9: Open Ledger and show generated linked blocks
        List<LedgerBlock> blocks = evidenceService.getLedgerService().getAllBlocks();
        assertEquals(3, blocks.size(), "Should have Genesis (0), Registered (1), and Transferred (2)");

        LedgerBlock block0 = blocks.get(0);
        LedgerBlock block1 = blocks.get(1);
        LedgerBlock block2 = blocks.get(2);

        assertEquals("GENESIS", block0.getAction());
        assertEquals("REGISTERED", block1.getAction());
        assertEquals("TRANSFERRED", block2.getAction());

        assertEquals(block0.getBlockHash(), block1.getPreviousHash(), "Block 1 must point to Block 0");
        assertEquals(block1.getBlockHash(), block2.getPreviousHash(), "Block 2 must point to Block 1");

        // Step 10 & 11: Click Verify Evidence -> Show "INTEGRITY VERIFIED"
        IntegrityVerificationResult integrityResult = evidenceService.verifyEvidenceIntegrity(registered);
        assertTrue(integrityResult.isVerified());
        assertEquals(IntegrityVerificationResult.Status.INTEGRITY_VERIFIED, integrityResult.getStatus());
        System.out.println("Demo Step 11: " + integrityResult.getMessage());

        // Step 12 & 13: Click Verify Ledger -> Show "LEDGER VALID"
        LedgerVerificationResult ledgerResult = evidenceService.getLedgerService().verifyLedger();
        assertTrue(ledgerResult.isValid());
        assertEquals(3, ledgerResult.getTotalBlocks());
        System.out.println("Demo Step 13: " + ledgerResult.getStatusMessage());

        // BONUS: Tamper detection demonstration
        Files.write(demoFile.toPath(), (demoContent + "_TAMPERED_BITS").getBytes());
        IntegrityVerificationResult tamperedCheck = evidenceService.verifyEvidenceIntegrity(registered);
        assertFalse(tamperedCheck.isVerified());
        assertEquals(IntegrityVerificationResult.Status.TAMPER_DETECTED, tamperedCheck.getStatus());
        System.out.println("Bonus Tamper Check: " + tamperedCheck.getMessage());
    }
}
