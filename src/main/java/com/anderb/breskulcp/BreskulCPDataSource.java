package com.anderb.breskulcp;

import com.anderb.breskulcp.exception.ConnectionException;
import com.anderb.breskulcp.exception.ConnectionTimeoutException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class BreskulCPDataSource extends BaseDataSource {
    private final BlockingQueue<Connection> pool;
    private final String jdbcUrl;
    private final String driverClassName;
    private final String username;
    private final char[] passwordChars;
    private final int poolSize;
    private final long connectionTimeout;

    public BreskulCPDataSource(DataSourceConfigs configs) {
        Objects.requireNonNull(configs);
        poolSize = configs.getPoolSize();
        connectionTimeout = configs.getConnectionTimeout();
        jdbcUrl = configs.getJdbcUrl();
        driverClassName = configs.getDriverClassName();
        username = configs.getUsername();
        passwordChars = configs.getPassword().toCharArray();
        pool = new LinkedBlockingQueue<>(poolSize);
        fillPool(poolSize);
    }

    @Override
    public Connection getConnection() {
        try {
            var connection = pool.poll(connectionTimeout, MILLISECONDS);
            if (connection == null) throw new ConnectionTimeoutException();
            return connection;
        } catch (InterruptedException e) {
            throw new ConnectionTimeoutException();
        }
    }

    void returnConnection(Connection connection) {
        boolean isValid = validate(connection);
        if (isValid) {
            pool.add(connection);
        } else {
            pool.add(createPooledConnection());
        }
    }

    private boolean validate(Connection connection) {
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private void fillPool(int poolSize) {
        for (int i = 0; i < poolSize; i++) {
            pool.add(createPooledConnection());
        }
    }

    private PooledConnection createPooledConnection() {
        try {
            Class.forName(driverClassName);
            Connection physicalConnection = DriverManager.getConnection(jdbcUrl, username, new String(passwordChars));
            return new PooledConnection(physicalConnection, this);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ConnectionException();
        }
    }

}
