package com.example.tenant.controller;

import com.example.tenant.config.MultitenantDataSource;
import com.example.tenant.entity.master.DataSourceConfig;
import com.example.tenant.repo.master.DataSourceConfigRepository;
import com.example.tenant.service.TenantManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tenants")
public class TenantManagementController {

    @Autowired
    private TenantManagementService tenantManagementService;

    @Autowired
    private DataSourceConfigRepository dataSourceConfigRepository;

    @Autowired
    private MultitenantDataSource multitenantDataSource;

    @PostMapping
    public ResponseEntity<String> createTenant(@RequestBody DataSourceConfig dataSourceConfig) {
        // 1. Save tenant config to master DB
        dataSourceConfigRepository.save(dataSourceConfig);

        // 2. Create new datasource
        DataSource newDataSource = tenantManagementService.createDataSource(dataSourceConfig);

        // 3. Add new datasource to the multitenant data source
        Map<Object, Object> resolvedDataSources = new HashMap<>(multitenantDataSource.getResolvedDataSources());
        resolvedDataSources.put(dataSourceConfig.getTenantId(), newDataSource);
        multitenantDataSource.setTargetDataSources(resolvedDataSources);
        multitenantDataSource.afterPropertiesSet(); // Re-initialize the data source

        // 4. Run liquibase migration on the new tenant's DB
        tenantManagementService.runLiquibase(newDataSource, "classpath:db/changelog/db.changelog-tenant.yaml");

        return ResponseEntity.ok("Tenant " + dataSourceConfig.getTenantId() + " created successfully.");
    }
}
