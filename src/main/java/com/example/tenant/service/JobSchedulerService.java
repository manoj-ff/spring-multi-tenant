package com.example.tenant.service;

import com.example.tenant.config.TenantContext;
import com.example.tenant.entity.master.TenantConfig;
import com.example.tenant.repo.master.TenantConfigRepository;
import com.example.tenant.repo.tenant.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobSchedulerService {

    private final TenantConfigRepository tenantConfigRepository;
    private final EmployeeRepository employeeRepository;

    @Scheduled(fixedDelay = 5000L)
    public void ping() {
        log.info("Pinging..");
        List<TenantConfig> tenants = tenantConfigRepository.findAll();

        for (TenantConfig tenant : tenants) {
            String tenantId = tenant.getTenantId();
            TenantContext.setCurrentTenant(tenantId);

            long count = employeeRepository.count();
            log.info("Tenant {} has {} employees", tenant.getTenantId(), count);

        }
    }
}
