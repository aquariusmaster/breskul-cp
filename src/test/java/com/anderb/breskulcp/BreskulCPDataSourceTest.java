package com.anderb.breskulcp;

import com.anderb.breskulcp.exception.ConnectionTimeoutException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class BreskulCPDataSourceTest {

    private static DataSource wrappedDatasource;

    @BeforeAll
    private static void init() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:testdb");
        ds.setUser("sa");
        ds.setPassword("sa");
        wrappedDatasource = ds;
    }

    @Test
    void getConnection_validateSuccessfulDatabaseConnection() throws Exception {
        BreskulCPDataSource breskulCPDataSource = new BreskulCPDataSource(wrappedDatasource);
        Connection connection = breskulCPDataSource.getConnection();
        Statement statement = connection.createStatement();
        boolean execute = statement.execute("select 1");
        connection.close();
        assertInstanceOf(PooledConnection.class, connection);
        assertTrue(execute);
    }

    @Test
    void getConnection_whenPoolIsEmptyAndConnectionTimeoutIsOver_throwConnectionTimeoutException() throws Exception {
        int poolSize = 1;
        BreskulCPDataSource breskulCPDataSource = new BreskulCPDataSource(wrappedDatasource, poolSize, 5);
        Connection connection = breskulCPDataSource.getConnection();
        assertThrows(ConnectionTimeoutException.class, breskulCPDataSource::getConnection);
        connection.close();
    }

    @Test
    void getConnection_whenCallGetConnection_shouldNotClosePhysicalConnectionAndUseCachedConnection() throws Exception {
        int poolSize = 1;
        BreskulCPDataSource breskulCPDataSource = new BreskulCPDataSource(wrappedDatasource, poolSize);
        Connection connection1 = breskulCPDataSource.getConnection();
        connection1.close();
        Connection connection2 = breskulCPDataSource.getConnection();
        connection2.close();

        assertEquals(connection1, connection2);
    }


}