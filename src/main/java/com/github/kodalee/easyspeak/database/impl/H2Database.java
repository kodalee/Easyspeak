package com.github.kodalee.easyspeak.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.github.kodalee.easyspeak.database.Database;
import com.github.kodalee.easyspeak.database.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class H2Database implements Database {

    private HikariDataSource dataSource;

    @Override
    public void connect() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:h2:file:./plugins/Easyspeak/" + DatabaseConfig.h2File());
        config.setDriverClassName("org.h2.Driver");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void disconnect() {
        if (dataSource != null) dataSource.close();
    }
}