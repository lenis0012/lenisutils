package com.lenis0012.pluginutils.sql;

import lombok.SneakyThrows;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SqlPooledConnection implements PooledConnection {
    private long lastUsedTime;
    private final Connection physicalConnection;
    private final Connection proxyConnection;
    private ConnectionEventListener connectionEventListener;

    public SqlPooledConnection(Connection physicalConnection, ClassLoader classLoader) {
        this.physicalConnection = physicalConnection;
        this.proxyConnection = (Connection) Proxy.newProxyInstance(classLoader, new Class[]{ Connection.class }, (proxy, method, args) -> {
            if(method.getName().equals("close")) {
                // Return connection to pool
                this.lastUsedTime = System.currentTimeMillis();
                if(connectionEventListener != null) {
                    connectionEventListener.connectionClosed(new ConnectionEvent(this));
                }
                return null;
            } else {
                try {
                    return method.invoke(physicalConnection, args);
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof SQLException) {
                        if(connectionEventListener != null) {
                            connectionEventListener.connectionErrorOccurred(new ConnectionEvent(this, (SQLException) e.getCause()));
                        }
                    }

                    throw e.getCause();
                }
            }
        });
    }

    @SneakyThrows
    public boolean isValid(int timeout) {
        return physicalConnection.isValid(timeout);
    }

    public boolean isClosed() {
        try {
            return physicalConnection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    public long getLastUsedTime() {
        return lastUsedTime;
    }

    public Connection getPhysicalConnection() {
        return physicalConnection;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return proxyConnection;
    }

    @Override
    public void close() throws SQLException {
        this.connectionEventListener = null;
        physicalConnection.close();
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        this.connectionEventListener = listener;
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        this.connectionEventListener = null;
    }

    @Override
    public void addStatementEventListener(StatementEventListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeStatementEventListener(StatementEventListener listener) {
        throw new UnsupportedOperationException();
    }
}
