package com.github.kodalee.easyspeak.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.github.kodalee.easyspeak.database.Database;
import com.github.kodalee.easyspeak.database.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class MariaDatabase implements Database {

    private HikariDataSource dataSource;

    @Override
    public void connect() {

        HikariConfig config = new HikariConfig();

        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setJdbcUrl(
                "jdbc:mariadb://" +
                        DatabaseConfig.host() + ":" +
                        DatabaseConfig.port() + "/" +
                        DatabaseConfig.database()
        );

        config.setUsername(DatabaseConfig.username());
        config.setPassword(DatabaseConfig.password());
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