package com.example.tenant;

import com.example.tenant.entity.master.AppUser;
import com.example.tenant.entity.master.AppUserRole;
import com.example.tenant.entity.master.DataSourceConfig;
import com.example.tenant.entity.tenant.Employee;
import com.example.tenant.repo.master.AppUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class MultiTenancyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeAll
    static void setup(@Autowired DataSource masterDataSource) {
        // Manually run master liquibase migration
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(masterDataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.yaml");
        try {
            liquibase.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException("Failed to run master liquibase migration", e);
        }
    }

    @Test
    void testTenantCreationAndDataIsolation() throws Exception {
        // 1. Create tenant1
        DataSourceConfig tenant1Config = createTenantConfig("tenant1");
        mockMvc.perform(post("/api/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tenant1Config)))
                .andExpect(status().isOk());

        // 2. Create tenant2
        DataSourceConfig tenant2Config = createTenantConfig("tenant2");
        mockMvc.perform(post("/api/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tenant2Config)))
                .andExpect(status().isOk());

        // 3. Create users for each tenant
        createTestUser("user1", "tenant1");
        createTestUser("user2", "tenant2");

        // 4. Add employee for tenant1
        Employee employee1 = new Employee();
        employee1.setId(1);
        employee1.setName("Employee One");
        mockMvc.perform(post("/employees")
                .with(httpBasic("user1", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee1)))
                .andExpect(status().isOk());

        // 5. Add employee for tenant2
        Employee employee2 = new Employee();
        employee2.setId(2);
        employee2.setName("Employee Two");
        mockMvc.perform(post("/employees")
                .with(httpBasic("user2", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee2)))
                .andExpect(status().isOk());

        // 6. Verify data isolation
        // As user1, should only see employee1
        mockMvc.perform(get("/employees")
                .with(httpBasic("user1", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Employee One"))
                .andExpect(jsonPath("$[1]").doesNotExist());

        // As user2, should only see employee2
        mockMvc.perform(get("/employees")
                .with(httpBasic("user2", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Employee Two"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    private DataSourceConfig createTenantConfig(String tenantId) {
        DataSourceConfig config = new DataSourceConfig();
        config.setTenantId(tenantId);
        config.setUrl("jdbc:h2:mem:" + tenantId + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        return config;
    }

    private void createTestUser(String username, String tenantId) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword("password"); // In a real app, this would be encoded
        user.setEnabled(true);
        user.setLocked(false);
        user.setAppUserRole(AppUserRole.USER);
        user.setTenantId(tenantId);
        appUserRepository.save(user);
    }
}
