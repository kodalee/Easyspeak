package com.github.kodalee.easyspeak.database.entity;

import java.util.UUID;

public record IgnoreEntry(UUID ignoredUuid, String ignoredName, long createdAt) {}
