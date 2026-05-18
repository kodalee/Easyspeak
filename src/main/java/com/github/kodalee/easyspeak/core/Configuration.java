package com.github.kodalee.easyspeak.core;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

public class Configuration {
    private static Configuration instance;

    private FileConfiguration config;
    private Server server;

    private Configuration(Server server) {
        this.server = server;
    }

    public static void init(Server server) {
        if (instance != null) {
            throw new RuntimeException("Configuration already initialized.");
        }
        instance = new Configuration(server);
    }

    public static Configuration getInstance() {
        if (instance == null) {
            throw new RuntimeException("Configuration not yet initialized.");
        }
        return instance;
    }

    public static Configuration initOrGet(Server server) {
        if (instance != null) {
            instance = new Configuration(server);
        }

        return instance;
    }

    public boolean load(JavaPlugin plugin) {
        if (!plugin.getDataFolder().exists()) {
            boolean mkdResult = plugin.getDataFolder().mkdirs();
            if (!mkdResult) {
                plugin.getLogger().severe("Failed to create the data folder for Aether.");
                return false;
            }
        }

        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        try {
            config = YamlConfiguration.loadConfiguration(configFile);
        }
        catch (Exception exception) {
            plugin.getLogger().severe("Something went wrong! A stack trace will now be printed");
            plugin.getLogger().severe("Error details: " + exception.toString());
            return false;
        }

        return true;
    }

    public Object get(String path) {
        return config.get(path);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public Location getLocation(String path) {
        World world = server.getWorld(config.getString(path + ".world", "world"));

        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");

        float yaw = (float) config.getDouble(path + ".yaw");
        float pitch = (float) config.getDouble(path + ".pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public String format(String path, Map<String, String> replacements) {
        String value = config.getString(path);
        if (value == null) return null;

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            value = value.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return value;
    }
}