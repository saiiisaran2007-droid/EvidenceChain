package com.evidencechain.repository;

import com.evidencechain.database.DatabaseManager;
import com.evidencechain.model.CustodyEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for CustodyEvent entity persistence using SQLite JDBC and PreparedStatements.
 */
public class CustodyRepository {

    /**
     * Saves a new CustodyEvent record into SQLite.
     *
     * @param event the custody event to save
     * @return the generated event ID
     * @throws SQLException if database error occurs
     */
    public long save(CustodyEvent event) throws SQLException {
        String sql = """
            INSERT INTO custody_events (
                evidence_id, action, actor_from, actor_to, timestamp, notes
            ) VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, event.getEvidenceId());
            ps.setString(2, event.getAction());
            ps.setString(3, event.getActorFrom());
            ps.setString(4, event.getActorTo() != null ? event.getActorTo() : "");
            ps.setString(5, event.getTimestamp());
            ps.setString(6, event.getNotes() != null ? event.getNotes() : "");

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    event.setEventId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Finds all custody events for a specific evidence item, ordered chronologically.
     */
    public List<CustodyEvent> findByEvidenceId(String evidenceId) throws SQLException {
        String sql = "SELECT * FROM custody_events WHERE evidence_id = ? ORDER BY timestamp ASC, event_id ASC";
        List<CustodyEvent> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, evidenceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCustodyEvent(rs));
                }
            }
        }
        return list;
    }

    /**
     * Finds recent custody events across all evidence items.
     */
    public List<CustodyEvent> findRecent(int limit) throws SQLException {
        String sql = "SELECT * FROM custody_events ORDER BY timestamp DESC, event_id DESC LIMIT ?";
        List<CustodyEvent> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCustodyEvent(rs));
                }
            }
        }
        return list;
    }

    /**
     * Gets the latest action recorded for a specific evidence item.
     */
    public String getLatestAction(String evidenceId) throws SQLException {
        String sql = "SELECT action FROM custody_events WHERE evidence_id = ? ORDER BY timestamp DESC, event_id DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, evidenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("action");
                }
            }
        }
        return "REGISTERED";
    }

    /**
     * Gets total number of custody events in the database.
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM custody_events";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private CustodyEvent mapResultSetToCustodyEvent(ResultSet rs) throws SQLException {
        return new CustodyEvent(
                rs.getLong("event_id"),
                rs.getString("evidence_id"),
                rs.getString("action"),
                rs.getString("actor_from"),
                rs.getString("actor_to"),
                rs.getString("timestamp"),
                rs.getString("notes")
        );
    }
}
