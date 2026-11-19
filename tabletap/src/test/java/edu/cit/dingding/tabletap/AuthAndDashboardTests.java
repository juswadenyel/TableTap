package edu.cit.dingding.tabletap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:tabletap-tests;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.jwt.secret=test-secret-that-is-more-than-thirty-two-characters-long",
        "app.jwt.expiration-minutes=60",
        "app.seed.owner-email=",
        "app.seed.owner-password=",
        "app.seed.staff-email=",
        "app.seed.staff-password="
})
@AutoConfigureMockMvc
class AuthAndDashboardTests {
    @Autowired private MockMvc mvc;

    @Test
    void registerSuccess() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"New Diner\",\"email\":\"new-diner@example.com\",\"phone\":\"1234567\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        String body = "{\"fullName\":\"Diner\",\"email\":\"duplicate@example.com\",\"password\":\"password123\"}";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void badLoginReturnsUnauthorized() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"missing@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void dinerCannotAccessOwnerDashboard() throws Exception {
        MvcResult registration = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Test Diner\",\"email\":\"dashboard-diner@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        Matcher tokenMatcher = Pattern.compile("\"token\":\"([^\"]+)\"")
                .matcher(registration.getResponse().getContentAsString());
        if (!tokenMatcher.find()) throw new AssertionError("Registration response did not contain a token");
        String token = tokenMatcher.group(1);

        mvc.perform(get("/api/dashboard/owner").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
