package com.example.tenant;

import com.example.tenant.entity.master.TenantConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MultiTenancyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminAuthToken;
    private static String tenant1AuthToken;
    private static String tenant2AuthToken;

    @Test
    @Order(1)
    void adminLogin() throws Exception {
        JSONObject credentials = new JSONObject();
        credentials.put("username", "admin@test.com");
        credentials.put("password", "pass");

        MvcResult mvcResult = mockMvc.perform(post("/api/security/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials.toString()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        adminAuthToken = "Bearer " + jsonNode.get("accessToken").asText();
    }

    @Test
    @Order(2)
    void createTenant1() throws Exception {
        TenantConfig tenant1Config = createTenantConfig("tenant1");
        mockMvc.perform(post("/api/tenants")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tenant1Config)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void createAppUserForTenant1() throws Exception {
        JSONObject user = new JSONObject();
        user.put("name", "user1");
        user.put("tenantId", "tenant1");
        user.put("email", "user1@test.com");
        user.put("password", "pass");

        mockMvc.perform(post("/api/security/users")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void loginTenant1() throws Exception {
        JSONObject credentials = new JSONObject();
        credentials.put("username", "user1@test.com");
        credentials.put("password", "pass");

        MvcResult mvcResult = mockMvc.perform(post("/api/security/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials.toString()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        tenant1AuthToken = "Bearer " + jsonNode.get("accessToken").asText();
    }

    @Test
    @Order(5)
    void createEmployeeForTenant1() throws Exception {
        JSONObject employee = new JSONObject();
        employee.put("name", "Employee 1");

        mockMvc.perform(post("/api/employees")
                        .header(HttpHeaders.AUTHORIZATION, tenant1AuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employee.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    void createTenant2() throws Exception {
        TenantConfig tenant2Config = createTenantConfig("tenant2");
        mockMvc.perform(post("/api/tenants")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tenant2Config)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    void createAppUserForTenant2() throws Exception {
        JSONObject user = new JSONObject();
        user.put("name", "user2");
        user.put("tenantId", "tenant2");
        user.put("email", "user2@test.com");
        user.put("password", "pass");

        mockMvc.perform(post("/api/security/users")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    void loginTenant2() throws Exception {
        JSONObject credentials = new JSONObject();
        credentials.put("username", "user2@test.com");
        credentials.put("password", "pass");

        MvcResult mvcResult = mockMvc.perform(post("/api/security/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials.toString()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        tenant2AuthToken = "Bearer " + jsonNode.get("accessToken").asText();
    }

    @Test
    @Order(9)
    void createEmployeeForTenant2() throws Exception {
        JSONObject employee = new JSONObject();
        employee.put("name", "Employee 2");

        mockMvc.perform(post("/api/employees")
                        .header(HttpHeaders.AUTHORIZATION, tenant2AuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employee.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    void verifyDataIsolation() throws Exception {
        // Verifying as tenant1
        mockMvc.perform(get("/api/employees")
                        .header(HttpHeaders.AUTHORIZATION, tenant1AuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Employee 1"));

        // Verifying as tenant2
        mockMvc.perform(get("/api/employees")
                        .header(HttpHeaders.AUTHORIZATION, tenant2AuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Employee 2"));
    }

    private TenantConfig createTenantConfig(String tenantId) {
        TenantConfig config = new TenantConfig();
        config.setTenantId(tenantId);
        config.setUrl("jdbc:h2:mem:" + tenantId + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_UPPER=FALSE");
        config.setUsername("sa");
        config.setPassword("");
        return config;
    }
}
