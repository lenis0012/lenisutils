package com.lenis0012.pluginutils.sql;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.DataSource;
import java.io.Closeable;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionPoolDataSource implements DataSource, ConnectionEventListener, Closeable {
    private final Deque<SqlPooledConnection> connections = new LinkedBlockingDeque<>();
    private final Semaphore semaphore;
    private final Plugin plugin;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private long validationBypassThreshold;
    private final int maxPoolSize;
    private final BukkitTask maintenanceTask;
    private final Logger logger;
    private boolean closed = false;
    private AtomicInteger activeRequests = new AtomicInteger(0);
    private final Object shutdownMonitor = new Object();

    public ConnectionPoolDataSource(Plugin plugin, String jdbcDriver, String jdbcUrl, int minSize, int maxSize,
                                    String username, String password, int connectionTimeout, int validationBypassThreshold, long validateInterval) {
        this.semaphore = new Semaphore(maxSize);
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
            boolean validationNeeded = last != null && (System.currentTimeMillis() - last.getLastUsedTime()) > validationBypassThreshold;

            // Verify all connections
            while(validationNeeded) {
                SqlPooledConnection connection = connections.pollLast();
                if (connection == null) {
                    break;
                }
                if ((System.currentTimeMillis() - connection.getLastUsedTime()) > validateInterval) {
                    // Presumed active, return to end of pool
                    connections.addLast(connection);
                    break;
                }
                if (connection.isValid(5000)) {
                    connections.addFirst(connection);
                    break;
                }
                tryClose(connection);
            }
        }, 0, 20 * 30);
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

        activeRequests.incrementAndGet();
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            activeRequests.decrementAndGet();
            throw new SQLException("Interrupted while waiting for a connection.", e);
        }
        try {
            SqlPooledConnection connection = connections.pollFirst();
            if (connection == null) {
                connection = createConnection();
            } else if((System.currentTimeMillis() - connection.getLastUsedTime()) > validationBypassThreshold) {
                if (!connection.isValid(5000)) {
                    tryClose(connection);
                    connection = createConnection();
                }
            }
            return connection.getConnection();
        } catch (Exception e) {
            activeRequests.decrementAndGet();
            semaphore.release();
            throw e;
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void connectionClosed(ConnectionEvent event) {
        SqlPooledConnection connection = (SqlPooledConnection) event.getSource();
        if(connection.isClosed()) {
            semaphore.release();
            tryClose(connection);
            return;
        }

        connections.addFirst((SqlPooledConnection) event.getSource());
        semaphore.release();
        activeRequests.decrementAndGet();
        synchronized (shutdownMonitor) {
            shutdownMonitor.notify();
        }
    }

    @Override
    public void connectionErrorOccurred(ConnectionEvent event) {
    }

    @Override
    public void close() {
        this.closed = true;
        maintenanceTask.cancel();

        long deadline = System.currentTimeMillis() + 10000;
        while (activeRequests.get() > 0 && System.currentTimeMillis() < deadline) {
            synchronized (shutdownMonitor) {
                try {
                    shutdownMonitor.wait(Math.max(1, deadline - System.currentTimeMillis()));
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "Interrupted while waiting for connections to close.", e);
                }
            }
        }

        SqlPooledConnection connection;
        while((connection = connections.pollFirst()) != null) {
            tryClose(connection);
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
