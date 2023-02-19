package com.lenis0012.pluginutils.sql;

import org.bukkit.plugin.Plugin;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.File;

public class DataSourceBuilder {
    private Plugin plugin;
    private String driver;
    private String jdbcUrlTemplate;
    private String hostname = "localhost";
    private int port;
    private String username = "root";
    private String password;
    private String database = "minecraft";
    private int minPoolSize = 1;
    private int maxPoolSize = 4;
    private int connectionTimeout = 10_000;
    private long validateInterval = 60_000;
    private int validationBypassThreshold = 500;

    public static DataSource sqlite(Plugin plugin, File file) {
        String jdbcUrl = "jdbc:sqlite:" + file.getPath();
        return new LockingDataSource(plugin, "org.sqlite.JDBC", jdbcUrl);
    }

    public static DataSourceBuilder mysqlBuilder(Plugin plugin) {
        DataSourceBuilder builder = new DataSourceBuilder();
        builder.plugin = plugin;
        builder.driver = "com.mysql.jdbc.Driver";
        builder.jdbcUrlTemplate = "jdbc:mysql://%1$s:%2$s/%3$s";
        builder.port = 3306;
        return builder;
    }

    public DataSourceBuilder hostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public DataSourceBuilder port(int port) {
        this.port = port;
        return this;
    }

    public DataSourceBuilder username(String username) {
        this.username = username;
        return this;
    }

    public DataSourceBuilder password(String password) {
        this.password = password;
        return this;
    }

    public DataSourceBuilder database(String database) {
        this.database = database;
        return this;
    }

    public DataSourceBuilder minPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
        return this;
    }

    public DataSourceBuilder maxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public DataSourceBuilder connectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public DataSourceBuilder validateInterval(long validateInterval) {
        this.validateInterval = validateInterval;
        return this;
    }

    public DataSourceBuilder validationBypassThreshold(int validationBypassThreshold) {
        this.validationBypassThreshold = validationBypassThreshold;
        return this;
    }

    public DataSource build() {
        String jdbcUrl = String.format(jdbcUrlTemplate, hostname, Integer.toString(port), database);
        return new ConnectionPoolDataSource(plugin, driver, jdbcUrl, minPoolSize, maxPoolSize, username, password, connectionTimeout, validationBypassThreshold, validateInterval);
    }
}
