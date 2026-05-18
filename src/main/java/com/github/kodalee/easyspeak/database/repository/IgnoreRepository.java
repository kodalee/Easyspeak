package com.github.kodalee.easyspeak.database.repository;

import com.github.kodalee.easyspeak.Easyspeak;
import com.github.kodalee.easyspeak.database.DatabaseManager;
import com.github.kodalee.easyspeak.database.entity.IgnoreEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IgnoreRepository {

    private final DatabaseManager db;

    public IgnoreRepository(DatabaseManager db) {
        this.db = db;
    }

    public void init() {
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement()) {

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ignored_players (" +
                            "  ignorer_uuid VARCHAR(36) NOT NULL," +
                            "  ignored_uuid VARCHAR(36) NOT NULL," +
                            "  ignored_name VARCHAR(32) NOT NULL," +
                            "  created_at BIGINT NOT NULL," +
                            "  PRIMARY KEY (ignorer_uuid, ignored_uuid)" +
                            ")"
            );
            st.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_ignored_players_ignored " +
                            "ON ignored_players (ignored_uuid)"
            );
        } catch (SQLException e) {
            Easyspeak.logger.severe("Failed to initialize ignored_players table: " + e.getMessage());
        }
    }

    public boolean add(UUID ignorer, UUID ignored, String ignoredName, long timestamp) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO ignored_players (ignorer_uuid, ignored_uuid, ignored_name, created_at) " +
                             "VALUES (?, ?, ?, ?)"
             )) {
            ps.setString(1, ignorer.toString());
            ps.setString(2, ignored.toString());
            ps.setString(3, ignoredName);
            ps.setLong(4, timestamp);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Duplicate-key (already ignored) is treated as a no-op success.
            return false;
        }
    }

    public boolean remove(UUID ignorer, UUID ignored) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM ignored_players WHERE ignorer_uuid = ? AND ignored_uuid = ?"
             )) {
            ps.setString(1, ignorer.toString());
            ps.setString(2, ignored.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Easyspeak.logger.severe("Failed to remove ignore: " + e.getMessage());
            return false;
        }
    }

    public Set<UUID> getIgnoredUuids(UUID ignorer) {
        Set<UUID> results = new HashSet<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT ignored_uuid FROM ignored_players WHERE ignorer_uuid = ?"
             )) {
            ps.setString(1, ignorer.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(UUID.fromString(rs.getString("ignored_uuid")));
            }
        } catch (SQLException e) {
            Easyspeak.logger.severe("Failed to load ignore set: " + e.getMessage());
        }
        return results;
    }

    public List<IgnoreEntry> list(UUID ignorer) {
        List<IgnoreEntry> results = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT ignored_uuid, ignored_name, created_at FROM ignored_players " +
                             "WHERE ignorer_uuid = ? ORDER BY created_at ASC"
             )) {
            ps.setString(1, ignorer.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new IgnoreEntry(
                            UUID.fromString(rs.getString("ignored_uuid")),
                            rs.getString("ignored_name"),
                            rs.getLong("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            Easyspeak.logger.severe("Failed to list ignores: " + e.getMessage());
        }
        return results;
    }
}
