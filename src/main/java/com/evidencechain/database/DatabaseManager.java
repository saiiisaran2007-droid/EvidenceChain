package com.evidencechain.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database connection and automated schema initialization.
 * Automatically creates tables and ensures foreign key support on launch.
 */
public class DatabaseManager {

    private static final String DEFAULT_DB_FILE = "evidencechain.db";
    private static String dbUrl = "jdbc:sqlite:" + DEFAULT_DB_FILE;

    private DatabaseManager() {
        // Utility class with static access
    }

    /**
     * Configures a custom database file path (useful for testing).
     */
    public static void setDatabasePath(String filePath) {
        dbUrl = "jdbc:sqlite:" + filePath;
    }

    /**
     * Gets a new connection to the SQLite database.
     * Foreign keys are enabled on every connection.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Automatically initializes the SQLite database schema if tables do not exist.
     * Creates evidence, custody_events, and ledger_blocks tables.
     */
    public static void initializeDatabase() {
        String createEvidenceTable = """
            CREATE TABLE IF NOT EXISTS evidence (
                evidence_id TEXT PRIMARY KEY,
                case_id TEXT NOT NULL,
                evidence_type TEXT NOT NULL,
                description TEXT,
                collector TEXT NOT NULL,
                source_device TEXT NOT NULL,
                recorded_at TEXT NOT NULL,
                file_path TEXT NOT NULL,
                file_size INTEGER NOT NULL DEFAULT 0,
                sha256_hash TEXT NOT NULL
            );
        """;

        String createCustodyEventsTable = """
            CREATE TABLE IF NOT EXISTS custody_events (
                event_id INTEGER PRIMARY KEY AUTOINCREMENT,
                evidence_id TEXT NOT NULL,
                action TEXT NOT NULL,
                actor_from TEXT NOT NULL,
                actor_to TEXT,
                timestamp TEXT NOT NULL,
                notes TEXT,
                FOREIGN KEY (evidence_id) REFERENCES evidence (evidence_id) ON DELETE CASCADE
            );
        """;

        String createLedgerBlocksTable = """
            CREATE TABLE IF NOT EXISTS ledger_blocks (
                block_index INTEGER PRIMARY KEY,
                timestamp TEXT NOT NULL,
                evidence_id TEXT NOT NULL,
                action TEXT NOT NULL,
                data_hash TEXT NOT NULL,
                previous_hash TEXT NOT NULL,
                block_hash TEXT NOT NULL
            );
        """;

        String createIndices = """
            CREATE INDEX IF NOT EXISTS idx_custody_evidence ON custody_events (evidence_id);
            CREATE INDEX IF NOT EXISTS idx_ledger_evidence ON ledger_blocks (evidence_id);
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createEvidenceTable);
            stmt.execute(createCustodyEventsTable);
            stmt.execute(createLedgerBlocksTable);
            stmt.execute(createIndices);
            System.out.println("[DatabaseManager] Database initialized successfully at: " + getDatabaseLocation());
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Failed to initialize database: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Checks if the database is accessible and connected.
     */
    public static boolean isConnected() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Returns the absolute path of the active SQLite database file.
     */
    public static String getDatabaseLocation() {
        String path = dbUrl.replace("jdbc:sqlite:", "");
        return new File(path).getAbsolutePath();
    }
}
