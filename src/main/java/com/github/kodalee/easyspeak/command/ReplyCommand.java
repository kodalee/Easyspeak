package com.github.kodalee.easyspeak.command;

import com.github.kodalee.easyspeak.Easyspeak;
import com.github.kodalee.easyspeak.core.PrivateMessageDispatcher;
import com.github.kodalee.easyspeak.shared.SharedConstants;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public class ReplyCommand implements CommandExecutor {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MM.deserialize("<red>Only players can reply to private messages."));
            return true;
        }

        if (!player.hasPermission("easyspeak.reply")) {
            player.sendMessage(MM.deserialize(Easyspeak.config.getString("messages.common.no_permission")));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(MM.deserialize("<red>Usage: /" + label + " <message>"));
            return true;
        }

        UUID targetUuid = SharedConstants.lastReplyTarget.get(player.getUniqueId());
        if (targetUuid == null) {
            player.sendMessage(MM.deserialize("<red>You have no one to reply to."));
            return true;
        }

        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null || !target.isOnline()) {
            player.sendMessage(MM.deserialize("<red>The player you were messaging is no longer online."));
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
        PrivateMessageDispatcher.send(player, target, message);
        return true;
    }
}
