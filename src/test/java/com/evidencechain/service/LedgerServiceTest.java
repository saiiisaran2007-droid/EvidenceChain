package com.evidencechain.service;

import com.evidencechain.database.DatabaseManager;
import com.evidencechain.model.LedgerBlock;
import com.evidencechain.model.LedgerVerificationResult;
import com.evidencechain.repository.LedgerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LedgerServiceTest {

    private static final String TEST_DB = "test_ledger_service.db";
    private LedgerService ledgerService;
    private LedgerRepository ledgerRepository;

    @BeforeEach
    void setUp() {
        DatabaseManager.setDatabasePath(TEST_DB);
        DatabaseManager.initializeDatabase();
        ledgerRepository = new LedgerRepository();
        ledgerService = new LedgerService(ledgerRepository);
    }

    @AfterEach
    void tearDown() {
        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    void testGenesisBlockAutoCreated() throws SQLException {
        List<LedgerBlock> blocks = ledgerRepository.findAll();
        assertFalse(blocks.isEmpty(), "Genesis block must be automatically created");
        LedgerBlock genesis = blocks.get(0);
        assertEquals(0, genesis.getBlockIndex(), "Genesis block index must be 0");
        assertEquals("GENESIS", genesis.getEvidenceId());
        assertEquals("0000000000000000000000000000000000000000000000000000000000000000", genesis.getPreviousHash());
        assertNotNull(genesis.getBlockHash());
        assertEquals(64, genesis.getBlockHash().length());
    }

    @Test
    void testRecordBlocksAndHashChaining() throws SQLException {
        LedgerBlock b1 = ledgerService.recordBlock("EV-0001", "REGISTERED", "datahash_111111");
        assertEquals(1, b1.getBlockIndex());
        assertEquals("EV-0001", b1.getEvidenceId());
        assertEquals("REGISTERED", b1.getAction());

        LedgerBlock genesis = ledgerRepository.findByIndex(0).orElseThrow();
        assertEquals(genesis.getBlockHash(), b1.getPreviousHash(), "Block #1 previousHash must equal Genesis blockHash");

        LedgerBlock b2 = ledgerService.recordBlock("EV-0001", "TRANSFERRED", "datahash_222222");
        assertEquals(2, b2.getBlockIndex());
        assertEquals(b1.getBlockHash(), b2.getPreviousHash(), "Block #2 previousHash must equal Block #1 blockHash");

        // Verify ledger validity
        LedgerVerificationResult verification = ledgerService.verifyLedger();
        assertTrue(verification.isValid(), "Intact blockchain-style ledger must verify as VALID");
        assertEquals(3, verification.getTotalBlocks());
        assertTrue(verification.getStatusMessage().contains("LEDGER VALID"));
    }

    @Test
    void testLedgerTamperDetectionWhenBlockContentModified() throws SQLException {
        ledgerService.recordBlock("EV-0001", "REGISTERED", "datahash_1");
        ledgerService.recordBlock("EV-0001", "TRANSFERRED", "datahash_2");

        // Tamper directly with Block 1 in SQLite database
        String tamperSql = "UPDATE ledger_blocks SET action = 'MALICIOUS_ALTERATION' WHERE block_index = 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(tamperSql)) {
            ps.executeUpdate();
        }

        LedgerVerificationResult verification = ledgerService.verifyLedger();
        assertFalse(verification.isValid(), "Modified block content must cause verification to fail");
        assertEquals(1, verification.getFailedBlockIndex(), "Failed block index must be Block #1");
        assertTrue(verification.getStatusMessage().contains("LEDGER COMPROMISED"));
    }

    @Test
    void testLedgerTamperDetectionWhenChainBroken() throws SQLException {
        ledgerService.recordBlock("EV-0001", "REGISTERED", "datahash_1");
        ledgerService.recordBlock("EV-0001", "TRANSFERRED", "datahash_2");

        // Tamper with previous_hash pointer in Block 2
        String tamperSql = "UPDATE ledger_blocks SET previous_hash = 'ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff' WHERE block_index = 2";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(tamperSql)) {
            ps.executeUpdate();
        }

        LedgerVerificationResult verification = ledgerService.verifyLedger();
        assertFalse(verification.isValid(), "Broken previousHash link must cause verification to fail");
        assertEquals(2, verification.getFailedBlockIndex(), "Failed block index must be Block #2");
        assertTrue(verification.getStatusMessage().contains("LEDGER COMPROMISED"));
    }
}
