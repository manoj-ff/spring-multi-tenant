package com.example.tenant;

import com.example.tenant.entity.master.AppUser;
import com.example.tenant.entity.master.AppUserRole;
import com.example.tenant.entity.master.DataSourceConfig;
import com.example.tenant.entity.tenant.Employee;
import com.example.tenant.repo.master.AppUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
public class MultiTenancyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    //@Test
    void testTenantCreationAndDataIsolation() throws Exception {
        // 1. Create tenant1
        DataSourceConfig tenant1Config = createTenantConfig("tenant1");
        mockMvc.perform(post("/api/tenants")
                        .with(httpBasic("user1", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tenant1Config)))
                .andExpect(status().isOk());

        // 2. Create tenant2
        DataSourceConfig tenant2Config = createTenantConfig("tenant2");
        mockMvc.perform(post("/api/tenants")
                        .with(httpBasic("user2", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tenant2Config)))
                .andExpect(status().isOk());

        // 3. Add employee for tenant1
        Employee employee1 = new Employee();
        employee1.setName("Employee One");
        mockMvc.perform(post("/api/employees")
                        .with(httpBasic("user1", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee1)))
                .andExpect(status().isOk());

        // 4. Add employee for tenant2
        Employee employee2 = new Employee();
        employee2.setName("Employee Two");
        mockMvc.perform(post("/api/employees")
                        .with(httpBasic("user2", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee2)))
                .andExpect(status().isOk());

        // 5. Verify data isolation
        // As user1, should only see employee1
        mockMvc.perform(get("/api/employees")
                        .with(httpBasic("user1", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Employee One"));

        // As user2, should only see employee2
        mockMvc.perform(get("/api/employees")
                        .with(httpBasic("user2", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Employee Two"));
    }

    private DataSourceConfig createTenantConfig(String tenantId) {
        DataSourceConfig config = new DataSourceConfig();
        config.setTenantId(tenantId);
        config.setUrl("jdbc:h2:mem:" + tenantId + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_UPPER=FALSE");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        return config;
    }
}
