package com.github.kodalee.easyspeak.shared;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SharedConstants {
    public static boolean isDisabled = false;
    public static double slowChatDuration = 1L;
    public static boolean allowColors = false;
    public static String serverName = "unknown";
    public static Map<UUID, Long> lastChat = new HashMap<UUID, Long>();
    public static Map<UUID, UUID> lastReplyTarget = new HashMap<UUID, UUID>();
}
