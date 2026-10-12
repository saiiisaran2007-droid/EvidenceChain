package com.evidencechain.repository;

import com.evidencechain.database.DatabaseManager;
import com.evidencechain.model.Evidence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Evidence entity persistence using SQLite JDBC and PreparedStatements.
 */
public class EvidenceRepository {

    /**
     * Saves a new Evidence record into SQLite.
     *
     * @param evidence the Evidence item to save
     * @throws SQLException if a database error occurs
     */
    public void save(Evidence evidence) throws SQLException {
        String sql = """
            INSERT INTO evidence (
                evidence_id, case_id, evidence_type, description, collector,
                source_device, recorded_at, file_path, file_size, sha256_hash
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, evidence.getEvidenceId());
            ps.setString(2, evidence.getCaseId());
            ps.setString(3, evidence.getEvidenceType());
            ps.setString(4, evidence.getDescription() != null ? evidence.getDescription() : "");
            ps.setString(5, evidence.getCollector());
            ps.setString(6, evidence.getSourceDevice());
            ps.setString(7, evidence.getRecordedAt());
            ps.setString(8, evidence.getFilePath());
            ps.setLong(9, evidence.getFileSize());
            ps.setString(10, evidence.getSha256Hash());

            ps.executeUpdate();
        }
    }

    /**
     * Finds an evidence item by its unique ID.
     */
    public Optional<Evidence> findById(String evidenceId) throws SQLException {
        String sql = "SELECT * FROM evidence WHERE evidence_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, evidenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEvidence(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves all evidence items ordered by recording date descending.
     */
    public List<Evidence> findAll() throws SQLException {
        String sql = "SELECT * FROM evidence ORDER BY recorded_at DESC, evidence_id DESC";
        List<Evidence> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEvidence(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves recent evidence items up to a maximum limit.
     */
    public List<Evidence> findRecent(int limit) throws SQLException {
        String sql = "SELECT * FROM evidence ORDER BY recorded_at DESC, evidence_id DESC LIMIT ?";
        List<Evidence> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEvidence(rs));
                }
            }
        }
        return list;
    }

    /**
     * Checks if an evidence ID already exists in the database.
     */
    public boolean existsById(String evidenceId) throws SQLException {
        String sql = "SELECT 1 FROM evidence WHERE evidence_id = ? LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, evidenceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Gets the total count of registered evidence items.
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM evidence";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Gets the count of distinct cases.
     */
    public int countDistinctCases() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT case_id) FROM evidence";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Calculates the next sequential number for generating EV-xxxx IDs.
     * Looks at existing EV-xxxx records to guarantee no collisions.
     */
    public int getNextEvidenceSequenceNumber() throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTR(evidence_id, 4) AS INTEGER)) FROM evidence WHERE evidence_id LIKE 'EV-%'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int max = rs.getInt(1);
                return max + 1;
            }
        }
        return 1;
    }

    private Evidence mapResultSetToEvidence(ResultSet rs) throws SQLException {
        Evidence e = new Evidence();
        e.setEvidenceId(rs.getString("evidence_id"));
        e.setCaseId(rs.getString("case_id"));
        e.setEvidenceType(rs.getString("evidence_type"));
        e.setDescription(rs.getString("description"));
        e.setCollector(rs.getString("collector"));
        e.setSourceDevice(rs.getString("source_device"));
        e.setRecordedAt(rs.getString("recorded_at"));
        e.setFilePath(rs.getString("file_path"));
        e.setFileSize(rs.getLong("file_size"));
        e.setSha256Hash(rs.getString("sha256_hash"));
        return e;
    }
}
