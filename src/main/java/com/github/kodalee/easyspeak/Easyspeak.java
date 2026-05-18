package com.github.kodalee.easyspeak;

import com.github.kodalee.easyspeak.command.ChatHistoryCommand;
import com.github.kodalee.easyspeak.command.ClearChatCommand;
import com.github.kodalee.easyspeak.command.IgnoreCommand;
import com.github.kodalee.easyspeak.command.PrivateMessageCommand;
import com.github.kodalee.easyspeak.command.ReloadCommand;
import com.github.kodalee.easyspeak.command.ReplyCommand;
import com.github.kodalee.easyspeak.command.SlowChatCommand;
import com.github.kodalee.easyspeak.command.ToggleChatCommand;
import com.github.kodalee.easyspeak.core.Configuration;
import com.github.kodalee.easyspeak.database.repository.ChatRepository;
import com.github.kodalee.easyspeak.database.repository.IgnoreRepository;
import com.github.kodalee.easyspeak.database.Database;
import com.github.kodalee.easyspeak.database.DatabaseFactory;
import com.github.kodalee.easyspeak.database.DatabaseManager;
import com.github.kodalee.easyspeak.shared.InternalLanguage;
import com.github.kodalee.easyspeak.shared.SharedConstants;
import com.github.kodalee.easyspeak.event.PlayerChatListener;
import com.github.kodalee.easyspeak.event.PlayerConnectionListener;
import com.github.kodalee.easyspeak.initializer.CommandRegisters;
import com.github.kodalee.easyspeak.initializer.EventRegisters;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class Easyspeak extends JavaPlugin {
    public static Easyspeak plugin;
    public static Server server;
    public static Logger logger;
    public static Configuration config;
    public static DatabaseManager database;
    public static ChatRepository chatRepository;
    public static IgnoreRepository ignoreRepository;

    private CommandRegisters commandRegisters;
    private EventRegisters eventRegisters;

    @Override
    public void onEnable() {
        plugin = this;
        server = getServer();
        logger = getLogger();

        Configuration.init(server);
        config = Configuration.getInstance();

        try {
            config.load(this);

            SharedConstants.isDisabled = !config.getBoolean("settings.enabled");
            SharedConstants.slowChatDuration = config.getDouble("settings.slow_mode_time");
            SharedConstants.allowColors = config.getBoolean("settings.allow_colors");
            SharedConstants.serverName = config.getString("server", "unknown");
        } catch (Exception e) {
            logger.severe(InternalLanguage.CRITICAL_CONFIG_LOAD);
            server.getPluginManager().disablePlugin(this);
            return;
        }

        Database db = DatabaseFactory.create();
        database = new DatabaseManager(db);

        database.connect();

        chatRepository = new ChatRepository(database);
        chatRepository.init();

        ignoreRepository = new IgnoreRepository(database);
        ignoreRepository.init();

        this.commandRegisters = new CommandRegisters(server, this, Map.of(
                "easyspeakreload", new ReloadCommand(),
                "togglechat", new ToggleChatCommand(),
                "slowchat", new SlowChatCommand(),
                "clearchat", new ClearChatCommand(),
                "chathistory", new ChatHistoryCommand(),
                "pm", new PrivateMessageCommand(),
                "reply", new ReplyCommand(),
                "ignore", new IgnoreCommand()
        ));

        this.eventRegisters = new EventRegisters(server, this, List.of(
                new PlayerChatListener(),
                new PlayerConnectionListener()
        ));

        logger.info(InternalLanguage.PLUGIN_READY);
    }

    @Override
    public void onDisable() {
        logger.info(InternalLanguage.PLUGIN_DISABLE);

    }
}
