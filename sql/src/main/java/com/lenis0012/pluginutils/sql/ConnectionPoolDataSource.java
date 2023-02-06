package com.lenis0012.pluginutils.sql;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import java.io.Closeable;
import java.io.PrintWriter;
import java.sql.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionPoolDataSource implements DataSource, ConnectionEventListener, Closeable {
    private final BlockingDeque<SqlPooledConnection> connections = new LinkedBlockingDeque<>();
    private final AtomicInteger availableCapacity;
    private final Plugin plugin;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private long validationBypassThreshold;
    private final int maxPoolSize;
    private final BukkitTask maintenanceTask;
    private final Logger logger;
    private boolean closed = false;

    public ConnectionPoolDataSource(Plugin plugin, String jdbcDriver, String jdbcUrl, int minSize, int maxSize,
                                    String username, String password, int connectionTimeout, int validationBypassThreshold, long validateInterval) {
        this.availableCapacity = new AtomicInteger(maxSize);
        this.plugin = plugin;
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.validationBypassThreshold = validationBypassThreshold;
        this.maxPoolSize = maxSize;
        this.logger = plugin.getLogger();
        loadDriver(jdbcDriver);
        if (connectionTimeout >= 1000) {
            DriverManager.setLoginTimeout(connectionTimeout / 1000);
        }
        this.maintenanceTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            SqlPooledConnection last = connections.peekLast();
            boolean validationNeeded = last != null && last.getLastUsedTime() > System.currentTimeMillis() - validationBypassThreshold;

            // Verify all connections
            while(validationNeeded) {
                SqlPooledConnection connection = connections.pollLast();
                if (connection == null) {
                    break;
                }
                if (connection.getLastUsedTime() > System.currentTimeMillis() - validateInterval) {
                    // Presumed active, return to end of pool
                    connections.addLast(connection);
                    break;
                }
                if (connection.isValid(5000)) {
                    connections.addFirst(connection);
                    break;
                }
                availableCapacity.incrementAndGet();
                tryClose(connection);
            }

            if (availableCapacity.get() >= maxSize - minSize) {
                tryAddConnections(maxSize - minSize);
            }
        }, 0, 20 * 30);
    }

    private void tryAddConnections(int desiredCapacity) {
            try {
                while (availableCapacity.getAndDecrement() > desiredCapacity) {
                    SqlPooledConnection connection = createConnection();
                    connections.addFirst(connection);
                }
            } catch (SQLException e) {
                // ignore
            } finally {
                availableCapacity.incrementAndGet();
            }
    }

    private SqlPooledConnection createConnection() throws SQLException {
        Connection physicalConn = DriverManager.getConnection(jdbcUrl, username, password);
        SqlPooledConnection connection = new SqlPooledConnection(physicalConn, plugin.getClass().getClassLoader());
        connection.addConnectionEventListener(this);
        return connection;
    }

    private void loadDriver(String jdbcDriver) {
        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to connect to database due to missing driver: " + jdbcDriver);
        }
    }

    private void tryClose(SqlPooledConnection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            // ignore
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (closed) {
            throw new SQLException("Datasource is already closed.");
        }

        if (connections.isEmpty() && availableCapacity.get() > 0) {
            if (availableCapacity.getAndDecrement() > 0) {
                try {
                    SqlPooledConnection connection = createConnection();
                    return connection.getConnection();
                } catch (Exception e) {
                    availableCapacity.incrementAndGet();
                    throw e;
                }

            } else {
                availableCapacity.incrementAndGet();
            }
        }

        try {
            SqlPooledConnection connection = connections.pollFirst(60, TimeUnit.SECONDS);
            if (connection == null) {
                throw new RuntimeException("Couldn't retrieve a database connection in 60 seconds.");
            }
            if (connection.getLastUsedTime() < System.currentTimeMillis() - validationBypassThreshold) {
                if (!connection.isValid(5000)) {
                    connection.removeConnectionEventListener(this);
                    availableCapacity.incrementAndGet();
                    tryClose(connection);
                    return getConnection();
                }
            }

            return connection.getConnection();
        } catch (InterruptedException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void connectionClosed(ConnectionEvent event) {
        connections.addFirst((SqlPooledConnection) event.getSource());
    }

    @Override
    public void connectionErrorOccurred(ConnectionEvent event) {
        availableCapacity.incrementAndGet();
        SqlPooledConnection connection = (SqlPooledConnection) event.getSource();
        tryClose(connection);
    }

    @Override
    public void close() {
        maintenanceTask.cancel();
        try {
            while(availableCapacity.get() < maxPoolSize) {
                SqlPooledConnection connection = connections.pollFirst(10, TimeUnit.SECONDS);
                if (connection == null) {
                    throw new IllegalStateException("Failed to shutdown database connection. no response after 120 seconds.");
                }
                availableCapacity.incrementAndGet();
                tryClose(connection);
            }
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Failed to shutdown database connections during shutdown.");
        }
    }

    /*
     * DataSource methods
     */

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
