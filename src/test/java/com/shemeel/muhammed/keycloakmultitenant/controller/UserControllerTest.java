package com.shemeel.muhammed.keycloakmultitenant.controller;

import com.shemeel.muhammed.keycloakmultitenant.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String TEST_TENANT = "tenant1";

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(TEST_TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void healthEndpointShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/public/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").value("Multi-tenant Keycloak service is running"));
    }

    @Test
    void getUserProfileShouldReturnUserDetails() throws Exception {
        // Create JWT token claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("preferred_username", "testuser");
        claims.put("email", "test@example.com");
        claims.put("given_name", "Test");
        claims.put("family_name", "User");
        claims.put("realm_access", Collections.singletonMap("roles", Collections.singletonList("user")));

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("test-subject")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        mockMvc.perform(get("/api/user/profile")
                        .header(TENANT_HEADER, TEST_TENANT)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tenantId").value(TEST_TENANT))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    void getAdminUsersShouldReturnForbiddenForNonAdminUser() throws Exception {
        Map<String, Object> claims = new HashMap<>();
        claims.put("preferred_username", "testuser");
        claims.put("realm_access", Collections.singletonMap("roles", Collections.singletonList("user")));

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("test-subject")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        mockMvc.perform(get("/api/admin/users")
                        .header(TENANT_HEADER, TEST_TENANT)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTenantInfoShouldReturnCurrentTenant() throws Exception {
        Map<String, Object> claims = new HashMap<>();
        claims.put("preferred_username", "testuser");
        claims.put("realm_access", Collections.singletonMap("roles", Collections.singletonList("user")));

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("test-subject")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        mockMvc.perform(get("/api/tenant/info")
                        .header(TENANT_HEADER, TEST_TENANT)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentTenant").value(TEST_TENANT))
                .andExpect(jsonPath("$.message").value("Current tenant information"));
    }
} 