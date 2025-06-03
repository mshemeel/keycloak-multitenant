package com.shemeel.muhammed.keycloakmultitenant.security;

import com.shemeel.muhammed.keycloakmultitenant.config.KeycloakTenantProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.JwtException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultiTenantJwtDecoderTest {

    private MultiTenantJwtDecoder multiTenantJwtDecoder;

    @Mock
    private KeycloakTenantProperties tenantProperties;

    private static final String TEST_TENANT = "tenant1";
    private static final String TEST_ISSUER = "http://localhost:8081/realms/tenant1";
    private static final String TEST_JWK_URI = "http://localhost:8081/realms/tenant1/protocol/openid-connect/certs";

    @BeforeEach
    void setUp() {
        multiTenantJwtDecoder = new MultiTenantJwtDecoder(tenantProperties);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldThrowExceptionWhenTenantContextNotSet() {
        // Given
        TenantContext.clear();
        String token = "dummy.jwt.token";

        // When/Then
        JwtException exception = assertThrows(JwtException.class,
                () -> multiTenantJwtDecoder.decode(token));
        assertEquals("Tenant ID not found in request context", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTenantNotFound() {
        // Given
        TenantContext.setCurrentTenant("unknown-tenant");
        String token = "dummy.jwt.token";

        // Setup tenant properties
        Map<String, KeycloakTenantProperties.TenantConfig> tenants = new HashMap<>();
        when(tenantProperties.getTenants()).thenReturn(tenants);

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> multiTenantJwtDecoder.decode(token));
        assertEquals("Unknown tenant: unknown-tenant", exception.getMessage());
    }

    @Test
    void shouldCreateDecoderForValidTenant() {
        // Given
        TenantContext.setCurrentTenant(TEST_TENANT);
        String token = "dummy.jwt.token";

        // Setup tenant properties
        KeycloakTenantProperties.TenantConfig tenantConfig = new KeycloakTenantProperties.TenantConfig();
        tenantConfig.setIssuerUri(TEST_ISSUER);
        tenantConfig.setJwkSetUri(TEST_JWK_URI);

        Map<String, KeycloakTenantProperties.TenantConfig> tenants = new HashMap<>();
        tenants.put(TEST_TENANT, tenantConfig);
        when(tenantProperties.getTenants()).thenReturn(tenants);

        // When/Then
        assertThrows(JwtException.class, () -> multiTenantJwtDecoder.decode(token),
                "Should throw JwtException for invalid token, but decoder creation should succeed");
    }
} 