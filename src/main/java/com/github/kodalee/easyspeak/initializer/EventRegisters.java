package com.github.kodalee.easyspeak.initializer;

import com.github.kodalee.easyspeak.Easyspeak;
import org.bukkit.Server;
import org.bukkit.event.Listener;

import java.util.List;

public class EventRegisters {
    private final Server server;
    private final Easyspeak plugin;

    public EventRegisters(Server server, Easyspeak plugin, List<Listener> events) {
        this.server = server;
        this.plugin = plugin;

        events.forEach(this::register);
    }

    private void register(Listener listener) {
        server.getPluginManager().registerEvents(listener, plugin);
    }
}
