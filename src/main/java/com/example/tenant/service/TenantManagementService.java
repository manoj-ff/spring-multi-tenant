package com.example.tenant.service;

import com.example.tenant.config.MultitenantDataSource;
import com.example.tenant.entity.master.DataSourceConfig;
import com.example.tenant.repo.master.DataSourceConfigRepository;
import jakarta.annotation.PostConstruct;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TenantManagementService {

    @Autowired
    private DataSourceConfigRepository dataSourceConfigRepository;

    @Autowired
    private MultitenantDataSource multitenantDataSource;

    @PostConstruct
    public void init() {
        Map<Object, Object> resolvedDataSources = new HashMap<>(multitenantDataSource.getResolvedDataSources());
        List<DataSourceConfig> tenantConfigs = dataSourceConfigRepository.findAll();
        for (DataSourceConfig config : tenantConfigs) {
            DataSource dataSource = createDataSource(config);
            resolvedDataSources.put(config.getTenantId(), dataSource);
        }
        multitenantDataSource.setTargetDataSources(resolvedDataSources);
        multitenantDataSource.afterPropertiesSet();
    }

    public List<DataSourceConfig> getAllTenantConfigs() {
        return dataSourceConfigRepository.findAll();
    }

    public DataSource createDataSource(DataSourceConfig config) {
        return DataSourceBuilder.create()
                .driverClassName(config.getDriverClassName())
                .url(config.getUrl())
                .username(config.getUsername())
                .password(config.getPassword())
                .build();
    }

    public void runLiquibase(DataSource dataSource, String changeLogPath) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLogPath);
        try {
            liquibase.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException("Failed to run liquibase migration", e);
        }
    }
}
