package com.github.kodalee.easyspeak.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface Database {

    void connect();

    Connection getConnection() throws SQLException;

    void disconnect();
}