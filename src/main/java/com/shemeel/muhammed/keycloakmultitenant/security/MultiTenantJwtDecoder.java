package com.shemeel.muhammed.keycloakmultitenant.security;

import com.shemeel.muhammed.keycloakmultitenant.config.KeycloakTenantProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MultiTenantJwtDecoder implements JwtDecoder {

    private final KeycloakTenantProperties tenantProperties;
    private final Map<String, JwtDecoder> jwtDecoders = new ConcurrentHashMap<>();

    @Autowired
    public MultiTenantJwtDecoder(KeycloakTenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        String tenantId = TenantContext.getCurrentTenant();

        if (tenantId == null) {
            throw new JwtException("Tenant ID not found in request context");
        }

        JwtDecoder jwtDecoder = jwtDecoders.computeIfAbsent(tenantId, this::createJwtDecoder);
        return jwtDecoder.decode(token);
    }

    private JwtDecoder createJwtDecoder(String tenantId) {
        KeycloakTenantProperties.TenantConfig tenantConfig = tenantProperties.getTenants().get(tenantId);

        if (tenantConfig == null) {
            throw new IllegalArgumentException("Unknown tenant: " + tenantId);
        }

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri(tenantConfig.getJwkSetUri())
                .build();

        // Add issuer validation
        jwtDecoder.setJwtValidator(createJwtValidator(tenantConfig.getIssuerUri()));

        return jwtDecoder;
    }

    private OAuth2TokenValidator<Jwt> createJwtValidator(String issuerUri) {
        return new JwtIssuerValidator(issuerUri);
    }
}
