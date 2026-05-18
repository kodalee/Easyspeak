package com.github.kodalee.easyspeak.command;

import com.github.kodalee.easyspeak.Easyspeak;
import com.github.kodalee.easyspeak.core.PrivateMessageDispatcher;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class PrivateMessageCommand implements CommandExecutor {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MM.deserialize("<red>Only players can send private messages."));
            return true;
        }

        if (!player.hasPermission("easyspeak.pm")) {
            player.sendMessage(MM.deserialize(Easyspeak.config.getString("messages.common.no_permission")));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(MM.deserialize("<red>Usage: /" + label + " <player> <message>"));
            return true;
        }

        Player recipient = Bukkit.getPlayerExact(args[0]);
        if (recipient == null || !recipient.isOnline()) {
            player.sendMessage(MM.deserialize(
                    "<red>Player <white>" + args[0] + "</white> is not online."));
            return true;
        }

        if (recipient.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(MM.deserialize("<red>You can't send a private message to yourself."));
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        PrivateMessageDispatcher.send(player, recipient, message);
        return true;
    }
}
