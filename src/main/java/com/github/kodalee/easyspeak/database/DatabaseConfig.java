package com.github.kodalee.easyspeak.database;

import com.github.kodalee.easyspeak.Easyspeak;

public class DatabaseConfig {

    public static String type() {
        return Easyspeak.config.getString("database.type");
    }

    public static String host() {
        return Easyspeak.config.getString("database.mariadb.host");
    }

    public static int port() {
        return Easyspeak.config.getInt("database.mariadb.port");
    }

    public static String database() {
        return Easyspeak.config.getString("database.mariadb.database");
    }

    public static String username() {
        return Easyspeak.config.getString("database.mariadb.username");
    }

    public static String password() {
        return Easyspeak.config.getString("database.mariadb.password");
    }

    public static String h2File() {
        return Easyspeak.config.getString("database.h2.file", "database");
    }
}