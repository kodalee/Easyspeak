package com.github.kodalee.easyspeak.core;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class IgnoreCache {

    private static final ConcurrentHashMap<UUID, Set<UUID>> CACHE = new ConcurrentHashMap<>();

    private IgnoreCache() {}

    public static boolean isIgnoring(UUID ignorer, UUID ignored) {
        Set<UUID> set = CACHE.get(ignorer);
        return set != null && set.contains(ignored);
    }

    public static void load(UUID ignorer, Set<UUID> ignored) {
        Set<UUID> set = ConcurrentHashMap.newKeySet();
        set.addAll(ignored);
        CACHE.put(ignorer, set);
    }

    public static void unload(UUID ignorer) {
        CACHE.remove(ignorer);
    }

    public static void add(UUID ignorer, UUID ignored) {
        CACHE.computeIfAbsent(ignorer, k -> ConcurrentHashMap.newKeySet()).add(ignored);
    }

    public static void remove(UUID ignorer, UUID ignored) {
        Set<UUID> set = CACHE.get(ignorer);
        if (set != null) set.remove(ignored);
    }
}
