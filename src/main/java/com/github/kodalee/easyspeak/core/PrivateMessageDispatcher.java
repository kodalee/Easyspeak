package com.github.kodalee.easyspeak.core;

import com.github.kodalee.easyspeak.Easyspeak;
import com.github.kodalee.easyspeak.shared.SharedConstants;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class PrivateMessageDispatcher {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private PrivateMessageDispatcher() {}

    public static void send(Player sender, Player recipient, String message) {
        if (IgnoreCache.isIgnoring(sender.getUniqueId(), recipient.getUniqueId())) {
            sender.sendMessage(MM.deserialize("<red>You can't send messages to a player you're ignoring. You can do /ignore <player> to unignore them."));
            return;
        }

        String renderedMessage = renderMessageBody(sender, message);

        String senderFormat = Easyspeak.config.getString("private_messages.sender_format");
        String recipientFormat = Easyspeak.config.getString("private_messages.recipient_format");

        String forSender = PlaceholderAPI.setPlaceholders(recipient, senderFormat)
                .replace("%message%", renderedMessage);
        String forRecipient = PlaceholderAPI.setPlaceholders(sender, recipientFormat)
                .replace("%message%", renderedMessage);

        sender.sendMessage(MM.deserialize(forSender));
        SharedConstants.lastReplyTarget.put(sender.getUniqueId(), recipient.getUniqueId());

        if (!IgnoreCache.isIgnoring(recipient.getUniqueId(), sender.getUniqueId())) {
            recipient.playSound(recipient, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
            recipient.sendMessage(MM.deserialize(forRecipient));
            SharedConstants.lastReplyTarget.put(recipient.getUniqueId(), sender.getUniqueId());
        }

        long now = System.currentTimeMillis();
        Easyspeak.server.getScheduler().runTaskAsynchronously(Easyspeak.plugin, () ->
                Easyspeak.chatRepository.savePrivate(
                        sender.getUniqueId(), sender.getName(),
                        recipient.getUniqueId(), recipient.getName(),
                        SharedConstants.serverName, message, now
                )
        );
    }

    private static String renderMessageBody(Player sender, String raw) {
        if (SharedConstants.allowColors && sender.hasPermission("easyspeak.colors")) {
            return MM.serialize(LEGACY.deserialize(raw));
        }
        // No color permission: pass through MM-safe so users can't inject MiniMessage tags.
        return MM.serialize(Component.text(raw));
    }
}
