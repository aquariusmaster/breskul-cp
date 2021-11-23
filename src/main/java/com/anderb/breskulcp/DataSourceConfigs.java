package com.anderb.breskulcp;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Data
public class DataSourceConfigs {
    private String jdbcUrl;
    private String driverClassName;
    private String username;
    private String password;
    private int poolSize = 10;
    private long connectionTimeout = 30_000;
}

