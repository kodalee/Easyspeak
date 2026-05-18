package com.github.kodalee.easyspeak.database;

import com.github.kodalee.easyspeak.database.impl.H2Database;
import com.github.kodalee.easyspeak.database.impl.MariaDatabase;

public class DatabaseFactory {

    public static Database create() {

        String type = DatabaseConfig.type();

        if ("MARIADB".equalsIgnoreCase(type)) {
            return new MariaDatabase();
        }

        return new H2Database();
    }
}