package com.shemeel.muhammed.keycloakmultitenant.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "keycloak")
@Getter
@Setter
public class KeycloakTenantProperties {

    private Map<String, TenantConfig> tenants;

    @Getter
    @Setter
    public static class TenantConfig {
        private String issuerUri;
        private String jwkSetUri;

    }
}
