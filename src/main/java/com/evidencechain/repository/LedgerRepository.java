package com.evidencechain.repository;

import com.evidencechain.database.DatabaseManager;
import com.evidencechain.model.LedgerBlock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LedgerBlock persistence in SQLite using JDBC and PreparedStatements.
 * Supports chronological chain retrieval and appending new hash-linked blocks.
 */
public class LedgerRepository {

    /**
     * Persists a new ledger block into SQLite.
     *
     * @param block the LedgerBlock to save
     * @throws SQLException if a database error occurs
     */
    public void save(LedgerBlock block) throws SQLException {
        String sql = """
            INSERT INTO ledger_blocks (
                block_index, timestamp, evidence_id, action, data_hash, previous_hash, block_hash
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, block.getBlockIndex());
            ps.setString(2, block.getTimestamp());
            ps.setString(3, block.getEvidenceId());
            ps.setString(4, block.getAction());
            ps.setString(5, block.getDataHash());
            ps.setString(6, block.getPreviousHash());
            ps.setString(7, block.getBlockHash());

            ps.executeUpdate();
        }
    }

    /**
     * Retrieves the latest block in the chain (highest block_index).
     */
    public Optional<LedgerBlock> getLatestBlock() throws SQLException {
        String sql = "SELECT * FROM ledger_blocks ORDER BY block_index DESC LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return Optional.of(mapResultSetToBlock(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves all blocks in the ledger ordered chronologically (block_index ASC).
     */
    public List<LedgerBlock> findAll() throws SQLException {
        String sql = "SELECT * FROM ledger_blocks ORDER BY block_index ASC";
        List<LedgerBlock> blocks = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                blocks.add(mapResultSetToBlock(rs));
            }
        }
        return blocks;
    }

    /**
     * Finds a block by its block_index.
     */
    public Optional<LedgerBlock> findByIndex(long blockIndex) throws SQLException {
        String sql = "SELECT * FROM ledger_blocks WHERE block_index = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, blockIndex);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBlock(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the total count of blocks in the ledger.
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM ledger_blocks";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private LedgerBlock mapResultSetToBlock(ResultSet rs) throws SQLException {
        return new LedgerBlock(
                rs.getLong("block_index"),
                rs.getString("timestamp"),
                rs.getString("evidence_id"),
                rs.getString("action"),
                rs.getString("data_hash"),
                rs.getString("previous_hash"),
                rs.getString("block_hash")
        );
    }
}
