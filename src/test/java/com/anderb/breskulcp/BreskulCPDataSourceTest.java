package com.anderb.breskulcp;

import com.anderb.breskulcp.exception.ConnectionTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class BreskulCPDataSourceTest {

    private DataSourceConfigs configs;

    @BeforeEach
    void init() {
        configs = DataSourceConfigs.builder()
                .jdbcUrl("jdbc:h2:mem:testdb")
                .username("sa")
                .password("sa")
                .driverClassName("org.h2.Driver")
                .poolSize(10)
                .connectionTimeout(30_000)
                .build();
    }

    @Test
    void getConnection_validateSuccessfulDatabaseConnection() throws Exception {
        BreskulCPDataSource breskulCPDataSource = new BreskulCPDataSource(configs);
        Connection connection = breskulCPDataSource.getConnection();
        Statement statement = connection.createStatement();
        boolean execute = statement.execute("select 1");
        connection.close();
        assertInstanceOf(PooledConnection.class, connection);
        assertTrue(execute);
    }

    @Test
    void getConnection_whenPoolIsEmptyAndConnectionTimeoutIsOver_throwConnectionTimeoutException() throws Exception {
        configs.setPoolSize(1);
        configs.setConnectionTimeout(5);
        BreskulCPDataSource breskulCPDataSource = new BreskulCPDataSource(configs);
        Connection connection = breskulCPDataSource.getConnection();
        assertThrows(ConnectionTimeoutException.class, breskulCPDataSource::getConnection);
        connection.close();
    }

    @Test
    void getConnection_whenCallGetConnection_shouldNotClosePhysicalConnectionAndUseCachedConnection() throws Exception {
        configs.setPoolSize(1);
        BreskulCPDataSource breskulCPDataSource = new BreskulCPDataSource(configs);
        Connection connection1 = breskulCPDataSource.getConnection();
        connection1.close();
        Connection connection2 = breskulCPDataSource.getConnection();
        connection2.close();

        assertEquals(connection1, connection2);
    }


}