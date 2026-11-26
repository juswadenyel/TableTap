package edu.cit.dingding.tabletap;

import com.jayway.jsonpath.JsonPath;
import edu.cit.dingding.tabletap.auth.model.PasswordResetToken;
import edu.cit.dingding.tabletap.auth.repository.PasswordResetTokenRepository;
import edu.cit.dingding.tabletap.user.model.User;
import edu.cit.dingding.tabletap.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// This integration suite covers profile, reset password, and change password flows.
@SpringBootTest
@AutoConfigureMockMvc
class Increment2Tests {
    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private PasswordResetTokenRepository resetTokens;

    private String register(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\" Test User \",\"email\":\"%s\",\"phone\":\"1234567\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isCreated()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private PasswordResetToken token(String email, String raw, LocalDateTime expiry) {
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(users.findByEmail(email).orElseThrow());
        token.setTokenHash(hash(raw));
        token.setExpiresAt(expiry);
        token.setCreatedAt(LocalDateTime.now());
        return resetTokens.save(token);
    }

    private String hash(String raw) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception exception) { throw new IllegalStateException(exception); }
    }

    @Test
    void profileIsOwnedByJwtAndPersistsEditableFields() throws Exception {
        String token = register("profile-" + UUID.randomUUID() + "@example.com", "password123");
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.passwordHash").doesNotExist()).andExpect(jsonPath("$.createdAt").exists());
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/users/me").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\" Updated Name \",\"email\":\"UPDATED@example.com\",\"phone\":\"+65 1234567\",\"role\":\"OWNER\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com")).andExpect(jsonPath("$.role").value("DINER"));
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token)).andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void invalidProfileAndDuplicateEmailAreRejected() throws Exception {
        String first = "profile-a-" + UUID.randomUUID() + "@example.com";
        String second = "profile-b-" + UUID.randomUUID() + "@example.com";
        String firstToken = register(first, "password123");
        String secondToken = register(second, "password123");
        mvc.perform(put("/api/users/me").header("Authorization", "Bearer " + firstToken).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\" \",\"email\":\"" + first + "\",\"phone\":\"bad\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.fullName").exists());
        UUID secondId = users.findByEmail(second).orElseThrow().getUserId();
        mvc.perform(put("/api/users/me").header("Authorization", "Bearer " + firstToken).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + secondId + "\",\"fullName\":\"Another\",\"email\":\"" + first + "\",\"phone\":\"1234567\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Another"));
        org.junit.jupiter.api.Assertions.assertEquals("Test User", users.findById(secondId).orElseThrow().getFullName());
        mvc.perform(put("/api/users/me").header("Authorization", "Bearer " + firstToken).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Another\",\"email\":\"" + second + "\",\"phone\":\"1234567\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void forgotPasswordIsGenericAndReplacesUnusedTokens() throws Exception {
        String email = "forgot-" + UUID.randomUUID() + "@example.com";
        register(email, "password123");
        String oldRaw = "prior-" + UUID.randomUUID();
        PasswordResetToken old = token(email, oldRaw, LocalDateTime.now().plusMinutes(15));
        MvcResult existing = mvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk()).andReturn();
        MvcResult missing = mvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"unknown-" + UUID.randomUUID() + "@example.com\"}"))
                .andExpect(status().isOk()).andReturn();
        org.junit.jupiter.api.Assertions.assertEquals(existing.getResponse().getContentAsString(), missing.getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertEquals("If an account exists, a reset link has been sent", JsonPath.read(existing.getResponse().getContentAsString(), "$.message"));
        mvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertNotNull(resetTokens.findById(old.getId()).orElseThrow().getUsedAt());
        var current = resetTokens.findByUserUserIdAndUsedAtIsNull(old.getUser().getUserId());
        org.junit.jupiter.api.Assertions.assertEquals(1, current.size());
        org.junit.jupiter.api.Assertions.assertEquals(64, current.get(0).getTokenHash().length());
        org.junit.jupiter.api.Assertions.assertNotEquals(oldRaw, resetTokens.findById(old.getId()).orElseThrow().getTokenHash());
    }

    @Test
    void resetPasswordIsSingleUseAndNewPasswordWorks() throws Exception {
        String email = "reset-" + UUID.randomUUID() + "@example.com";
        register(email, "password123");
        String raw = "reset-secret-" + UUID.randomUUID();
        token(email, raw, LocalDateTime.now().plusMinutes(15));
        mvc.perform(get("/api/auth/reset-password/validate").param("token", raw)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + raw + "\",\"newPassword\":\"newpassword123\",\"confirmPassword\":\"newpassword123\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + email + "\",\"password\":\"newpassword123\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + raw + "\",\"newPassword\":\"otherpass123\",\"confirmPassword\":\"otherpass123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void expiredAndInvalidTokensAndWeakOrMismatchedPasswordsFail() throws Exception {
        String email = "expired-" + UUID.randomUUID() + "@example.com";
        register(email, "password123");
        String expired = "expired-" + UUID.randomUUID();
        token(email, expired, LocalDateTime.now().minusMinutes(1));
        mvc.perform(get("/api/auth/reset-password/validate").param("token", expired)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/auth/reset-password/validate").param("token", "unknown-token")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"missing\",\"newPassword\":\"longenough\",\"confirmPassword\":\"mismatch\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("New passwords do not match"));
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"missing\",\"newPassword\":\"short\",\"confirmPassword\":\"short\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.newPassword").exists());
    }

    @Test
    void changePasswordRevokesCurrentJwt() throws Exception {
        String email = "change-" + UUID.randomUUID() + "@example.com";
        String token = register(email, "password123");
        mvc.perform(post("/api/users/me/change-password").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"wrong\",\"newPassword\":\"newpassword123\",\"confirmPassword\":\"newpassword123\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Current password is incorrect"));
        mvc.perform(post("/api/users/me/change-password").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"password123\",\"newPassword\":\"password123\",\"confirmPassword\":\"password123\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/users/me/change-password").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"password123\",\"newPassword\":\"short\",\"confirmPassword\":\"short\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/users/me/change-password").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"password123\",\"newPassword\":\"newpassword123\",\"confirmPassword\":\"different123\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/users/me/change-password").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"password123\",\"newPassword\":\"newpassword123\",\"confirmPassword\":\"newpassword123\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + email + "\",\"password\":\"newpassword123\"}"))
                .andExpect(status().isOk());
    }
}
