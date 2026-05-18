package com.github.kodalee.easyspeak.event;

import com.github.kodalee.easyspeak.Easyspeak;
import com.github.kodalee.easyspeak.core.IgnoreCache;
import com.github.kodalee.easyspeak.shared.SharedConstants;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class PlayerChatListener implements Listener {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        // Check if canceled as the
        // proxy may have gotten it first.
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        event.setCancelled(true);

        if (SharedConstants.isDisabled && !player.hasPermission("easyspeak.togglechat.bypass")) {
            player.sendMessage(MM.deserialize(Easyspeak.config.getString("messages.event.chat_disabled")));
            return;
        }

        long now = System.currentTimeMillis();

        if (SharedConstants.slowChatDuration != 0 && !player.hasPermission("easyspeak.slowchat.bypass")) {
            long lastChat = SharedConstants.lastChat.getOrDefault(uuid, 0L);

            long cooldownMs = (long) (SharedConstants.slowChatDuration * 1000.0);
            long nextCanChat = lastChat + cooldownMs;

            if (now < nextCanChat) {

                long remainingSeconds = (long) Math.ceil((nextCanChat - now) / 1000.0);

                player.sendMessage(
                        MM.deserialize(
                                Easyspeak.config.getString("messages.event.chat_slow")
                                        .replace("%duration%", String.valueOf(remainingSeconds))
                        )
                );
                return;
            }

            SharedConstants.lastChat.put(uuid, now);
        }

        String message = renderMessage(player, event);
        String preapplied = PlaceholderAPI.setPlaceholders(player, Easyspeak.config.getString("settings.format"));
        String format = preapplied.replace("%_message%", message);

        Component rendered = MM.deserialize(format);
        Easyspeak.server.getOnlinePlayers().forEach(p -> {
            if (IgnoreCache.isIgnoring(p.getUniqueId(), uuid)) return;
            p.sendMessage(rendered);
        });

        String plain = PlainTextComponentSerializer.plainText().serialize(event.message());
        Easyspeak.chatRepository.savePublic(uuid, player.getName(), SharedConstants.serverName, plain, now);
    }

    private String renderMessage(Player player, AsyncChatEvent event) {
        if (SharedConstants.allowColors && player.hasPermission("easyspeak.colors")) {
            String plain = PlainTextComponentSerializer.plainText().serialize(event.message());
            return MM.serialize(LEGACY.deserialize(plain));
        }
        return MM.serialize(event.message());
    }
}
