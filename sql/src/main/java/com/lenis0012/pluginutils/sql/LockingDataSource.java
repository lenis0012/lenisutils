package com.lenis0012.pluginutils.sql;

import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.DataSource;
import java.io.Closeable;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LockingDataSource implements DataSource, ConnectionEventListener, Closeable {
    private final Plugin plugin;
    private final String jdbcUrl;
    private final Logger logger;
    private SqlPooledConnection pooledConnection;
    private ReentrantLock lock = new ReentrantLock();
    private AtomicInteger activeRequests = new AtomicInteger(0);
    private boolean closed = false;
    private final Object shutdownMonitor = new Object();

    @SneakyThrows
    public LockingDataSource(Plugin plugin, String jdbcDriver, String jdbcUrl) {
        this.plugin = plugin;
        this.jdbcUrl = jdbcUrl;
        this.logger = plugin.getLogger();

        loadDriver(jdbcDriver);
        this.pooledConnection = createConnection();
    }

    private SqlPooledConnection createConnection() throws SQLException {
        Connection physicalConn = DriverManager.getConnection(jdbcUrl);
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
        lock.lock();
        return pooledConnection.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void connectionClosed(ConnectionEvent event) {
        activeRequests.decrementAndGet();
        lock.unlock();
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

        // Wait for all connections to be closed
        long deadline = System.currentTimeMillis() + 10000;
        while (activeRequests.get() > 0 && System.currentTimeMillis() < deadline) {
            synchronized (shutdownMonitor) {
                try {
                    shutdownMonitor.wait(5000);
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "Interrupted while waiting for connections to close.", e);
                }
            }
        }

        // Close connection
        tryClose(pooledConnection);
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
