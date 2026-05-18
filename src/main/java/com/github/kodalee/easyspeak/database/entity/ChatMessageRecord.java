package com.github.kodalee.easyspeak.database.entity;

import java.util.UUID;

public record ChatMessageRecord(
        long id,
        MessageType type,
        UUID uuid,
        String playerName,
        UUID recipientUuid,
        String recipientName,
        String server,
        String message,
        long timestamp
) {
    public boolean isPrivate() {
        return type == MessageType.PRIVATE;
    }
}
