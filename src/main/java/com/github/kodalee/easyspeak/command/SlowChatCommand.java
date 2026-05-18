package com.github.kodalee.easyspeak.command;

import com.github.kodalee.easyspeak.Easyspeak;
import com.github.kodalee.easyspeak.shared.SharedConstants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SlowChatCommand implements CommandExecutor {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("easyspeak.slowchat")) {
            sender.sendMessage(MM.deserialize(Easyspeak.config.getString("messages.common.no_permission")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(MM.deserialize("<red>Usage: /slowchat <seconds>"));
            sender.sendMessage(MM.deserialize("<red>- You can disable slow mode by entering 0 in seconds."));
            return true;
        }

        try {
            double newDuration = Double.parseDouble(args[0]);
            SharedConstants.slowChatDuration = newDuration;
            messageAllPlayers(newDuration == 0 ?
                    MM.deserialize("<yellow>Slow mode has been <red>disabled</red> for chat.") :
                    MM.deserialize("<yellow>Slow mode duration has been set to <b>" + newDuration + " seconds</b>.")
            );
        } catch (NumberFormatException e) {
            sender.sendMessage(MM.deserialize("<red>Usage: /slowchat <seconds>"));
            sender.sendMessage(MM.deserialize("<red>The number you enter must be a whole number, no decimals."));
        }

        return true;
    }

    private void messageAllPlayers(Component componentMessage) {
        Easyspeak.server.getOnlinePlayers().forEach(p -> p.sendMessage(componentMessage));
    }
}
