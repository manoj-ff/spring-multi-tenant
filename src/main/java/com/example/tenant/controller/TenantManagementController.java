package com.example.tenant.controller;

import com.example.tenant.config.MultitenantDataSource;
import com.example.tenant.entity.master.TenantConfig;
import com.example.tenant.repo.master.TenantConfigRepository;
import com.example.tenant.service.TenantManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private TenantConfigRepository tenantConfigRepository;

    @Autowired
    private MultitenantDataSource multitenantDataSource;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<String> createTenant(@RequestBody TenantConfig tenantConfig) {
        tenantConfigRepository.save(tenantConfig);
        DataSource newDataSource = tenantManagementService.createDataSource(tenantConfig);
        Map<Object, Object> resolvedDataSources = new HashMap<>(multitenantDataSource.getResolvedDataSources());
        resolvedDataSources.put(tenantConfig.getTenantId(), newDataSource);
        multitenantDataSource.setTargetDataSources(resolvedDataSources);
        multitenantDataSource.afterPropertiesSet();
        tenantManagementService.runLiquibase(newDataSource, "classpath:db/changelog/db.changelog-tenant.xml");

        return ResponseEntity.ok("Tenant " + tenantConfig.getTenantId() + " created successfully.");
    }
}
