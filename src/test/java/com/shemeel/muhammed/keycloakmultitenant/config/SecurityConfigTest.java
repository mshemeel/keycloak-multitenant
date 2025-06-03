package com.shemeel.muhammed.keycloakmultitenant.config;

import com.shemeel.muhammed.keycloakmultitenant.security.MultiTenantJwtDecoder;
import com.shemeel.muhammed.keycloakmultitenant.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MultiTenantJwtDecoder multiTenantJwtDecoder;

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
    void shouldAllowAccessToPublicEndpoints() throws Exception {
        mockMvc.perform(get("/api/public/health"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessToAuthenticatedUserWithValidToken() throws Exception {
        // Given
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

        when(multiTenantJwtDecoder.decode(anyString())).thenReturn(jwt);

        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer token")
                        .header(TENANT_HEADER, TEST_TENANT))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessToAdminEndpointForNonAdminUser() throws Exception {
        // Given
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

        when(multiTenantJwtDecoder.decode(anyString())).thenReturn(jwt);

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer token")
                        .header(TENANT_HEADER, TEST_TENANT))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAccessToAdminEndpointForAdminUser() throws Exception {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("preferred_username", "admin");
        claims.put("realm_access", Collections.singletonMap("roles", Arrays.asList("ROLE_ADMIN", "ROLE_USER")));

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("admin-subject")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt,
                Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
                )
        );

        mockMvc.perform(get("/api/admin/users")
                        .header(TENANT_HEADER, TEST_TENANT)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isOk());
    }
} 