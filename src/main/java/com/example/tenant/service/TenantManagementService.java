package com.example.tenant.service;

import com.example.tenant.config.MultitenantDataSource;
import com.example.tenant.entity.master.TenantConfig;
import com.example.tenant.repo.master.TenantConfigRepository;
import jakarta.annotation.PostConstruct;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@DependsOn("masterLiquibase")
public class TenantManagementService {

    @Autowired
    private TenantConfigRepository tenantConfigRepository;

    @Autowired
    private MultitenantDataSource multitenantDataSource;

    @PostConstruct
    public void init() {
        Map<Object, Object> resolvedDataSources = new HashMap<>(multitenantDataSource.getResolvedDataSources());
        List<TenantConfig> tenantConfigs = tenantConfigRepository.findAll();
        for (TenantConfig config : tenantConfigs) {
            DataSource dataSource = createDataSource(config);
            resolvedDataSources.put(config.getTenantId(), dataSource);
        }
        multitenantDataSource.setTargetDataSources(resolvedDataSources);
        multitenantDataSource.afterPropertiesSet();
    }

    public List<TenantConfig> getAllTenantConfigs() {
        return tenantConfigRepository.findAll();
    }

    public DataSource createDataSource(TenantConfig config) {
        return DataSourceBuilder.create()
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
