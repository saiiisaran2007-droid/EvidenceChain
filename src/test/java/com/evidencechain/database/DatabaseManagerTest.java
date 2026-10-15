package com.evidencechain.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    private static final String TEST_DB = "test_evidencechain.db";

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
    void testTablesCreatedSuccessfully() throws Exception {
        assertTrue(DatabaseManager.isConnected(), "Database should report connected");

        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            Set<String> tables = new HashSet<>();

            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME").toLowerCase());
                }
            }

            assertTrue(tables.contains("evidence"), "Table 'evidence' must exist");
            assertTrue(tables.contains("custody_events"), "Table 'custody_events' must exist");
            assertTrue(tables.contains("ledger_blocks"), "Table 'ledger_blocks' must exist");
        }
    }

    @Test
    void testPreparedStatementInserts() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            // 1. Insert into evidence table
            String insertEvidence = """
                INSERT INTO evidence (evidence_id, case_id, evidence_type, description, collector, source_device, recorded_at, file_path, file_size, sha256_hash)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(insertEvidence)) {
                ps.setString(1, "EV-TEST-001");
                ps.setString(2, "CASE-2026-01");
                ps.setString(3, "Disk Image");
                ps.setString(4, "Test Evidence Item");
                ps.setString(5, "Agent Smith");
                ps.setString(6, "Workstation-Alpha");
                ps.setString(7, "2026-09-30T20:00:00");
                ps.setString(8, "C:\\test\\evidence.raw");
                ps.setLong(9, 1024L);
                ps.setString(10, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
                int rows = ps.executeUpdate();
                assertEquals(1, rows, "Evidence row should be inserted");
            }

            // 2. Insert into custody_events table
            String insertEvent = """
                INSERT INTO custody_events (evidence_id, action, actor_from, actor_to, timestamp, notes)
                VALUES (?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(insertEvent)) {
                ps.setString(1, "EV-TEST-001");
                ps.setString(2, "Evidence Registered");
                ps.setString(3, "Agent Smith");
                ps.setString(4, "Forensics Lab Vault");
                ps.setString(5, "2026-09-30T20:01:00");
                ps.setString(6, "Initial intake secure custody");
                int rows = ps.executeUpdate();
                assertEquals(1, rows, "Custody event row should be inserted");
            }

            // 3. Insert into ledger_blocks table
            String insertBlock = """
                INSERT INTO ledger_blocks (block_index, timestamp, evidence_id, action, data_hash, previous_hash, block_hash)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(insertBlock)) {
                ps.setLong(1, 0);
                ps.setString(2, "2026-09-30T20:01:00");
                ps.setString(3, "EV-TEST-001");
                ps.setString(4, "GENESIS");
                ps.setString(5, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
                ps.setString(6, "0000000000000000000000000000000000000000000000000000000000000000");
                ps.setString(7, "a1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef0");
                int rows = ps.executeUpdate();
                assertEquals(1, rows, "Genesis ledger block should be inserted");
            }
        }
    }
}
