package com.github.kodalee.easyspeak.database.repository;

import com.github.kodalee.easyspeak.Easyspeak;
import com.github.kodalee.easyspeak.database.DatabaseManager;
import com.github.kodalee.easyspeak.database.entity.ChatMessageRecord;
import com.github.kodalee.easyspeak.database.entity.MessageType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ChatRepository {

    private final DatabaseManager db;

    public ChatRepository(DatabaseManager db) {
        this.db = db;
    }

    public void init() {
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement()) {

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS chat_messages (" +
                            "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                            "  message_type VARCHAR(16) NOT NULL DEFAULT 'PUBLIC'," +
                            "  player_uuid VARCHAR(36) NOT NULL," +
                            "  player_name VARCHAR(32) NOT NULL," +
                            "  recipient_uuid VARCHAR(36)," +
                            "  recipient_name VARCHAR(32)," +
                            "  server VARCHAR(64) NOT NULL DEFAULT 'unknown'," +
                            "  message TEXT NOT NULL," +
                            "  sent_at BIGINT NOT NULL" +
                            ")"
            );

            // Migrations for installs that pre-date these columns.
            st.executeUpdate(
                    "ALTER TABLE chat_messages " +
                            "ADD COLUMN IF NOT EXISTS server VARCHAR(64) NOT NULL DEFAULT 'unknown'"
            );
            st.executeUpdate(
                    "ALTER TABLE chat_messages " +
                            "ADD COLUMN IF NOT EXISTS message_type VARCHAR(16) NOT NULL DEFAULT 'PUBLIC'"
            );
            st.executeUpdate(
                    "ALTER TABLE chat_messages " +
                            "ADD COLUMN IF NOT EXISTS recipient_uuid VARCHAR(36)"
            );
            st.executeUpdate(
                    "ALTER TABLE chat_messages " +
                            "ADD COLUMN IF NOT EXISTS recipient_name VARCHAR(32)"
            );

            st.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_chat_messages_uuid_sent_at " +
                            "ON chat_messages (player_uuid, sent_at)"
            );
            st.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_chat_messages_name " +
                            "ON chat_messages (player_name)"
            );
            st.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_chat_messages_recipient_name " +
                            "ON chat_messages (recipient_name)"
            );
            st.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_chat_messages_sent_at " +
                            "ON chat_messages (sent_at)"
            );
            st.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_chat_messages_server_id " +
                            "ON chat_messages (server, id)"
            );
            st.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_chat_messages_pm_thread " +
                            "ON chat_messages (message_type, player_uuid, recipient_uuid, id)"
            );
        } catch (SQLException e) {
            Easyspeak.logger.severe("Failed to initialize chat_messages table: " + e.getMessage());
        }
    }

    public void savePublic(UUID uuid, String name, String server, String message, long timestamp) {
        insert(MessageType.PUBLIC, uuid, name, null, null, server, message, timestamp);
    }

    public void savePrivate(UUID senderUuid, String senderName, UUID recipientUuid, String recipientName,
                            String server, String message, long timestamp) {
        insert(MessageType.PRIVATE, senderUuid, senderName, recipientUuid, recipientName, server, message, timestamp);
    }

    private void insert(MessageType type, UUID uuid, String name, UUID recipientUuid, String recipientName,
                        String server, String message, long timestamp) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO chat_messages " +
                             "(message_type, player_uuid, player_name, recipient_uuid, recipient_name, server, message, sent_at) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
             )) {
            ps.setString(1, type.name());
            ps.setString(2, uuid.toString());
            ps.setString(3, name);
            if (recipientUuid != null) {
                ps.setString(4, recipientUuid.toString());
            } else {
                ps.setNull(4, java.sql.Types.VARCHAR);
            }
            if (recipientName != null) {
                ps.setString(5, recipientName);
            } else {
                ps.setNull(5, java.sql.Types.VARCHAR);
            }
            ps.setString(6, server);
            ps.setString(7, message);
            ps.setLong(8, timestamp);
            ps.executeUpdate();
        } catch (SQLException e) {
            Easyspeak.logger.severe("Failed to save chat message: " + e.getMessage());
        }
    }

    /** Counts messages where player is the sender, or the recipient of a PM. */
    public int countByName(String name) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM chat_messages " +
                             "WHERE LOWER(player_name) = LOWER(?) " +
                             "   OR (message_type = 'PRIVATE' AND LOWER(recipient_name) = LOWER(?))"
             )) {
            ps.setString(1, name);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            Easyspeak.logger.severe("Failed to count chat history: " + e.getMessage());
        }
        return 0;
    }

    public List<ChatMessageRecord> getHistoryByName(String name, int limit, int offset) {
        List<ChatMessageRecord> results = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     selectColumns() +
                             "FROM chat_messages " +
                             "WHERE LOWER(player_name) = LOWER(?) " +
                             "   OR (message_type = 'PRIVATE' AND LOWER(recipient_name) = LOWER(?)) " +
                             "ORDER BY id ASC LIMIT ? OFFSET ?"
             )) {
            ps.setString(1, name);
            ps.setString(2, name);
            ps.setInt(3, limit);
            ps.setInt(4, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(fromRow(rs));
            }
        } catch (SQLException e) {
            Easyspeak.logger.severe("Failed to fetch chat history: " + e.getMessage());
        }
        return results;
    }

    public ChatMessageRecord getById(long id) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     selectColumns() + "FROM chat_messages WHERE id = ?"
             )) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return fromRow(rs);
            }
        } catch (SQLException e) {
            Easyspeak.logger.severe("Failed to fetch chat message #" + id + ": " + e.getMessage());
        }
        return null;
    }

    /** Returns up to `limit` messages immediately preceding the target, scoped to the same conversation. */
    public List<ChatMessageRecord> getBeforeContext(ChatMessageRecord target, int limit) {
        List<ChatMessageRecord> results = new ArrayList<>();
        String sql;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = (target.isPrivate()
                     ? prepareThreadContext(conn, target, true, limit)
                     : preparePublicContext(conn, target, true, limit))) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(fromRow(rs));
            }
        } catch (SQLException e) {
            Easyspeak.logger.severe("Failed to fetch context-before for #" + target.id() + ": " + e.getMessage());
        }
        Collections.reverse(results);
        return results;
    }

    public List<ChatMessageRecord> getAfterContext(ChatMessageRecord target, int limit) {
        List<ChatMessageRecord> results = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = (target.isPrivate()
                     ? prepareThreadContext(conn, target, false, limit)
                     : preparePublicContext(conn, target, false, limit))) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(fromRow(rs));
            }
        } catch (SQLException e) {
            Easyspeak.logger.severe("Failed to fetch context-after for #" + target.id() + ": " + e.getMessage());
        }
        return results;
    }

    private PreparedStatement preparePublicContext(Connection conn, ChatMessageRecord target,
                                                   boolean before, int limit) throws SQLException {
        String comparator = before ? "<" : ">";
        String order = before ? "DESC" : "ASC";
        PreparedStatement ps = conn.prepareStatement(
                selectColumns() +
                        "FROM chat_messages " +
                        "WHERE id " + comparator + " ? " +
                        "  AND message_type = 'PUBLIC' " +
                        "  AND server = ? " +
                        "ORDER BY id " + order + " LIMIT ?"
        );
        ps.setLong(1, target.id());
        ps.setString(2, target.server());
        ps.setInt(3, limit);
        return ps;
    }

    private PreparedStatement prepareThreadContext(Connection conn, ChatMessageRecord target,
                                                   boolean before, int limit) throws SQLException {
        String comparator = before ? "<" : ">";
        String order = before ? "DESC" : "ASC";
        PreparedStatement ps = conn.prepareStatement(
                selectColumns() +
                        "FROM chat_messages " +
                        "WHERE id " + comparator + " ? " +
                        "  AND message_type = 'PRIVATE' " +
                        "  AND ((player_uuid = ? AND recipient_uuid = ?) " +
                        "    OR (player_uuid = ? AND recipient_uuid = ?)) " +
                        "ORDER BY id " + order + " LIMIT ?"
        );
        String a = target.uuid().toString();
        String b = target.recipientUuid() != null ? target.recipientUuid().toString() : "";
        ps.setLong(1, target.id());
        ps.setString(2, a);
        ps.setString(3, b);
        ps.setString(4, b);
        ps.setString(5, a);
        ps.setInt(6, limit);
        return ps;
    }

    private String selectColumns() {
        return "SELECT id, message_type, player_uuid, player_name, recipient_uuid, recipient_name, " +
                "server, message, sent_at ";
    }

    private ChatMessageRecord fromRow(ResultSet rs) throws SQLException {
        String recipientUuidStr = rs.getString("recipient_uuid");
        UUID recipientUuid = recipientUuidStr != null ? UUID.fromString(recipientUuidStr) : null;
        return new ChatMessageRecord(
                rs.getLong("id"),
                MessageType.valueOf(rs.getString("message_type")),
                UUID.fromString(rs.getString("player_uuid")),
                rs.getString("player_name"),
                recipientUuid,
                rs.getString("recipient_name"),
                rs.getString("server"),
                rs.getString("message"),
                rs.getLong("sent_at")
        );
    }
}
