package com.github.kodalee.easyspeak.initializer;

import com.github.kodalee.easyspeak.Easyspeak;
import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;

import java.util.Map;

public class CommandRegisters {
    private final Server server;
    private final Easyspeak plugin;

    public CommandRegisters(Server server, Easyspeak plugin, Map<String, CommandExecutor> commands) {
        this.server = server;
        this.plugin = plugin;

        commands.forEach(this::register);
    }

    public void register(String commandName, CommandExecutor commandExecutor) {
        PluginCommand command = plugin.getCommand(commandName);
        if (command == null) {
            Easyspeak.logger.info("Could not register command '" + commandName + "' as it doesn't exist!");
            return;
        }

        command.setExecutor(commandExecutor);
    }

}
