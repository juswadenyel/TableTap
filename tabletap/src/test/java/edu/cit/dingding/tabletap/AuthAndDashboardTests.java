package edu.cit.dingding.tabletap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAndDashboardTests {
    @Autowired private MockMvc mvc;

    private String register(String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{\"fullName\":\"  Test Diner  \",\"email\":\"%s\",\"phone\":\"+65 1234567\",\"password\":\"password123\"}".formatted(email)))
                .andExpect(status().isCreated())
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    @Test
    void registerSuccess() throws Exception {
        mvc.perform(post("/api/auth/register").contentType("application/json")
                        .content("{\"fullName\":\"New Diner\",\"email\":\"new-diner@example.com\",\"phone\":\"1234567\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.fullName").value("New Diner"));
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        String body = "{\"fullName\":\"Diner\",\"email\":\"duplicate@example.com\",\"password\":\"password123\"}";
        mvc.perform(post("/api/auth/register").contentType("application/json").content(body)).andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").contentType("application/json").content(body)).andExpect(status().isConflict());
    }

    @Test
    void badLoginReturnsUnauthorized() throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content("{\"email\":\"missing@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void dinerCannotAccessOwnerDashboard() throws Exception {
        String token = register("dashboard-diner@example.com");
        mvc.perform(get("/api/dashboard/owner").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.message").value("Forbidden"));
    }
}
