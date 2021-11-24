# breskul-cp
### Polled DataSource Implementation

## How to use it

First you should create a `DataSourceConfigs`:
```java
DataSourceConfigs configs = DataSourceConfigs.builder()
                .jdbcUrl("jdbc:h2:mem:testdb")
                .username("sa")
                .password("sa")
                .driverClassName("org.h2.Driver")
                .poolSize(15)
                .connectionTimeout(60_000)
                .build();
```
Pass configs to `BreskulCPDataSource`:
```java
BreskulCPDataSource breskulCPDataSource = new BreskulCPDataSource(configs);
```
Use a usual DataSource:
```java
try (Connection connection = breskulCPDataSource.getConnection()) {
    Statement statement = connection.createStatement();
    boolean execute = statement.execute("select 1");
} catch (SQLException e) {
}
```
