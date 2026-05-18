package com.github.kodalee.easyspeak.command;

import com.github.kodalee.easyspeak.Easyspeak;
import com.github.kodalee.easyspeak.core.IgnoreCache;
import com.github.kodalee.easyspeak.database.entity.IgnoreEntry;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class IgnoreCommand implements CommandExecutor {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MM.deserialize("<red>Only players can manage their ignore list."));
            return true;
        }

        if (!player.hasPermission("easyspeak.ignore")) {
            player.sendMessage(MM.deserialize(Easyspeak.config.getString("messages.common.no_permission")));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(MM.deserialize("<red>Usage: /" + label + " <player>"));
            player.sendMessage(MM.deserialize("<red>       /" + label + " list"));
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            handleList(player);
            return true;
        }

        handleToggle(player, args[0]);
        return true;
    }

    private void handleList(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(Easyspeak.plugin, () -> {
            List<IgnoreEntry> entries = Easyspeak.ignoreRepository.list(player.getUniqueId());
            if (entries.isEmpty()) {
                player.sendMessage(MM.deserialize("<gray>You aren't ignoring anyone."));
                return;
            }
            player.sendMessage(MM.deserialize(
                    "<gold>=== Ignored players <gray>(" + entries.size() + ")</gray> ==="));
            for (IgnoreEntry e : entries) {
                player.sendMessage(MM.deserialize(
                        "<white>" + e.ignoredName() + "</white> " +
                                "<dark_gray>since " + TS.format(Instant.ofEpochMilli(e.createdAt())) + "</dark_gray>"));
            }
        });
    }

    private void handleToggle(Player player, String targetName) {
        Bukkit.getScheduler().runTaskAsynchronously(Easyspeak.plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            if (target.getName() == null && !target.hasPlayedBefore()) {
                player.sendMessage(MM.deserialize(
                        "<red>No player named <white>" + targetName + "</white> has joined this server."));
                return;
            }

            UUID targetUuid = target.getUniqueId();
            String resolvedName = target.getName() != null ? target.getName() : targetName;

            if (targetUuid.equals(player.getUniqueId())) {
                player.sendMessage(MM.deserialize("<red>You can't ignore yourself."));
                return;
            }

            if (IgnoreCache.isIgnoring(player.getUniqueId(), targetUuid)) {
                Easyspeak.ignoreRepository.remove(player.getUniqueId(), targetUuid);
                IgnoreCache.remove(player.getUniqueId(), targetUuid);
                player.sendMessage(MM.deserialize(
                        "<green>You are no longer ignoring <white>" + resolvedName + "</white>."));
            } else {
                Easyspeak.ignoreRepository.add(
                        player.getUniqueId(), targetUuid, resolvedName, System.currentTimeMillis());
                IgnoreCache.add(player.getUniqueId(), targetUuid);
                player.sendMessage(MM.deserialize(
                        "<yellow>You are now ignoring <white>" + resolvedName + "</white>."));
            }
        });
    }
}
