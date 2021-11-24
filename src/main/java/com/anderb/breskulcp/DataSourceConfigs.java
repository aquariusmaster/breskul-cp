package com.anderb.breskulcp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataSourceConfigs {
    private String jdbcUrl;
    private String driverClassName;
    private String username;
    private String password;
    @Builder.Default
    private int poolSize = 10;
    @Builder.Default
    private long connectionTimeout = 30_000;
}

