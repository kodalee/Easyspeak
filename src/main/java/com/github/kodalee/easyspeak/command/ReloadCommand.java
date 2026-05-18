package com.github.kodalee.easyspeak.command;

import com.github.kodalee.easyspeak.Easyspeak;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("easyspeak.reload")) {
            sender.sendMessage(MM.deserialize(Easyspeak.config.getString("messages.common.no_permission")));
            return true;
        }

        if (Easyspeak.config.load(Easyspeak.plugin)) {
            sender.sendMessage(MM.deserialize("<green>Configuration was reloaded. Make sure to check console for any issues."));
        } else {
            sender.sendMessage(MM.deserialize("<red>An error occurred while reloading your configuration file. Please check console to see what went wrong."));
        }

        return true;
    }
}
