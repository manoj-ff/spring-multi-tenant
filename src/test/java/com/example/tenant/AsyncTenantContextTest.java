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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.example.tenant.repo.master.AppUserRepository;
import com.example.tenant.repo.master.TenantConfigRepository;
import org.junit.jupiter.api.AfterAll;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AsyncTenantContextTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static TenantConfigRepository tenantConfigRepository;

    private static AppUserRepository appUserRepository;

    @Autowired
    void setTenantConfigRepository(TenantConfigRepository tenantConfigRepository) {
        AsyncTenantContextTest.tenantConfigRepository = tenantConfigRepository;
    }

    @Autowired
    void setAppUserRepository(AppUserRepository appUserRepository) {
        AsyncTenantContextTest.appUserRepository = appUserRepository;
    }

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
    void createTenants() throws Exception {
        TenantConfig tenant1Config = createTenantConfig("tenant3");
        mockMvc.perform(post("/api/tenants")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tenant1Config)))
                .andExpect(status().isOk());

        TenantConfig tenant2Config = createTenantConfig("tenant4");
        mockMvc.perform(post("/api/tenants")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tenant2Config)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void createAppUsersForTenants() throws Exception {
        JSONObject user1 = new JSONObject();
        user1.put("name", "user3");
        user1.put("tenantId", "tenant3");
        user1.put("email", "user3@test.com");
        user1.put("password", "pass");

        mockMvc.perform(post("/api/security/users")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user1.toString()))
                .andExpect(status().isOk());

        JSONObject user2 = new JSONObject();
        user2.put("name", "user4");
        user2.put("tenantId", "tenant4");
        user2.put("email", "user4@test.com");
        user2.put("password", "pass");

        mockMvc.perform(post("/api/security/users")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user2.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void loginTenants() throws Exception {
        JSONObject credentials1 = new JSONObject();
        credentials1.put("username", "user3@test.com");
        credentials1.put("password", "pass");

        MvcResult mvcResult1 = mockMvc.perform(post("/api/security/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials1.toString()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode jsonNode1 = objectMapper.readTree(mvcResult1.getResponse().getContentAsString());
        tenant1AuthToken = "Bearer " + jsonNode1.get("accessToken").asText();

        JSONObject credentials2 = new JSONObject();
        credentials2.put("username", "user4@test.com");
        credentials2.put("password", "pass");

        MvcResult mvcResult2 = mockMvc.perform(post("/api/security/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials2.toString()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode jsonNode2 = objectMapper.readTree(mvcResult2.getResponse().getContentAsString());
        tenant2AuthToken = "Bearer " + jsonNode2.get("accessToken").asText();
    }

    @Test
    @Order(5)
    void createEmployeeAsyncForTenant1() throws Exception {
        JSONObject employee = new JSONObject();
        employee.put("name", "Async Employee 1");

        mockMvc.perform(post("/api/employees/async")
                        .header(HttpHeaders.AUTHORIZATION, tenant1AuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employee.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    void createEmployeeAsyncForTenant2() throws Exception {
        JSONObject employee = new JSONObject();
        employee.put("name", "Async Employee 2");

        mockMvc.perform(post("/api/employees/async")
                        .header(HttpHeaders.AUTHORIZATION, tenant2AuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employee.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    void verifyDataIsolation() throws Exception {
        // Verifying as tenant1
        mockMvc.perform(get("/api/employees")
                        .header(HttpHeaders.AUTHORIZATION, tenant1AuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Async Employee 1"));

        // Verifying as tenant2
        mockMvc.perform(get("/api/employees")
                        .header(HttpHeaders.AUTHORIZATION, tenant2AuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Async Employee 2"));
    }

    private TenantConfig createTenantConfig(String tenantId) {
        TenantConfig config = new TenantConfig();
        config.setTenantId(tenantId);
        config.setUrl("jdbc:h2:mem:" + tenantId + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_UPPER=FALSE");
        config.setUsername("sa");
        config.setPassword("");
        return config;
    }

    @AfterAll
    static void cleanup() {
        tenantConfigRepository.deleteByTenantId("tenant3");
        tenantConfigRepository.deleteByTenantId("tenant4");
        appUserRepository.deleteByTenantId("tenant3");
        appUserRepository.deleteByTenantId("tenant4");
    }
}
