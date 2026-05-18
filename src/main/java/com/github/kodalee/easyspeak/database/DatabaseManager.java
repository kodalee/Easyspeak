package com.github.kodalee.easyspeak.database;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {

    private final Database database;

    public DatabaseManager(Database database) {
        this.database = database;
    }

    public void connect() {
        database.connect();
    }

    public Connection getConnection() throws SQLException {
        return database.getConnection();
    }

    public void disconnect() {
        database.disconnect();
    }
}