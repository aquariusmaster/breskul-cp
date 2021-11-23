package com.anderb.breskulcp;

import com.anderb.breskulcp.exception.ConnectionException;
import com.anderb.breskulcp.exception.ConnectionTimeoutException;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class BreskulCPDataSource implements DataSource {
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
    public Connection getConnection() throws SQLException {
        try {
            var connection = pool.poll(connectionTimeout, MILLISECONDS);
            if (connection == null) throw new ConnectionTimeoutException();
            return connection;
        } catch (InterruptedException e) {
            throw new ConnectionTimeoutException();
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("Operation is not supported yet");
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Operation is not supported yet");
//        return delegateDatasource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
//        delegateDatasource.setLogWriter(out);
        throw new UnsupportedOperationException("Operation is not supported yet");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
//        return delegateDatasource.getLoginTimeout();
        throw new UnsupportedOperationException("Operation is not supported yet");
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
//        delegateDatasource.setLoginTimeout(seconds);
        throw new UnsupportedOperationException("Operation is not supported yet");
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
//        return delegateDatasource.getParentLogger();
        throw new UnsupportedOperationException("Operation is not supported yet");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
//        return delegateDatasource.unwrap(iface);
        throw new UnsupportedOperationException("Operation is not supported yet");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
//        return delegateDatasource.isWrapperFor(iface);
        throw new UnsupportedOperationException("Operation is not supported yet");
    }

    protected Queue<Connection> getPool() {
        return pool;
    }

    private void fillPool(int poolSize) {
        for (int i = 0; i < poolSize; i++) {
            pool.add(new PooledConnection(createConnection(), this));
        }
    }

    private Connection createConnection() {
        try {
            Class.forName(driverClassName);
            return DriverManager.getConnection(jdbcUrl, username, new String(passwordChars));
        } catch (ClassNotFoundException | SQLException e) {
            throw new ConnectionException();
        }
    }


}
