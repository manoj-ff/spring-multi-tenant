package com.example.tenant.repo.master;

import com.example.tenant.entity.master.DataSourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataSourceConfigRepository extends JpaRepository<DataSourceConfig, Long> {
    Optional<DataSourceConfig> findByTenantId(String tenantId);
}
