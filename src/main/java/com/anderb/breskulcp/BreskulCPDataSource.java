package com.anderb.breskulcp;

import com.anderb.breskulcp.exception.ConnectionException;
import com.anderb.breskulcp.exception.ConnectionTimeoutException;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class BreskulCPDataSource implements DataSource {
    private final DataSource delegateDatasource;
    private final BlockingQueue<Connection> pool;
    private final long connectionTimeout;

    public BreskulCPDataSource(DataSource delegateDatasource) {
        this(delegateDatasource, 10);
    }

    public BreskulCPDataSource(DataSource delegateDatasource, int poolSize) {
        this(delegateDatasource, poolSize, 30_000);
    }

    public BreskulCPDataSource(DataSource delegateDatasource, int poolSize, long connectionTimeout) {
        Objects.requireNonNull(delegateDatasource);
        if (poolSize < 1) throw new IllegalArgumentException("Pool size cannot be less then 1");
        this.delegateDatasource = delegateDatasource;
        this.connectionTimeout = connectionTimeout;
        pool = new LinkedBlockingQueue<>(poolSize);
        fillPool(poolSize);
    }

    private void fillPool(int poolSize) {
        try {
            for (int i = 0; i < poolSize; i++) {
                pool.add(new PooledConnection(delegateDatasource.getConnection(), this));
            }
        } catch (SQLException e) {
            throw new ConnectionException();
        }
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
        return delegateDatasource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegateDatasource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegateDatasource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegateDatasource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegateDatasource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegateDatasource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegateDatasource.isWrapperFor(iface);
    }

    public Queue<Connection> getPool() {
        return pool;
    }
}
