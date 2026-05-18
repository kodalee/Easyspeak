package com.github.kodalee.easyspeak.event;

import com.github.kodalee.easyspeak.Easyspeak;
import com.github.kodalee.easyspeak.core.IgnoreCache;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;
import java.util.UUID;

public class PlayerConnectionListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(Easyspeak.plugin, () -> {
            Set<UUID> ignored = Easyspeak.ignoreRepository.getIgnoredUuids(uuid);
            IgnoreCache.load(uuid, ignored);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        IgnoreCache.unload(event.getPlayer().getUniqueId());
    }
}
