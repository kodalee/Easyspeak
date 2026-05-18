package com.github.kodalee.easyspeak.command;

import com.github.kodalee.easyspeak.Easyspeak;
import com.github.kodalee.easyspeak.shared.SharedConstants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ToggleChatCommand implements CommandExecutor {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("easyspeak.togglechat")) {
            sender.sendMessage(MM.deserialize(Easyspeak.config.getString("messages.common.no_permission")));
            return true;
        }

        SharedConstants.isDisabled = !SharedConstants.isDisabled;
        messageAllPlayers(MM.deserialize(
                SharedConstants.isDisabled ?
                        Easyspeak.config.getString("messages.broadcast.chat_toggled_off") :
                        Easyspeak.config.getString("messages.broadcast.chat_toggled_on")
        ));

        return true;
    }

    private void messageAllPlayers(Component componentMessage) {
        Easyspeak.server.getOnlinePlayers().forEach(p -> p.sendMessage(componentMessage));
    }
}
