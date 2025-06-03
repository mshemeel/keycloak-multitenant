package com.shemeel.muhammed.keycloakmultitenant.controller;

import com.shemeel.muhammed.keycloakmultitenant.security.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/public/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Multi-tenant Keycloak service is running");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();

        Map<String, Object> profile = new HashMap<>();
        profile.put("tenantId", TenantContext.getCurrentTenant());
        profile.put("username", jwt.getClaimAsString("preferred_username"));
        profile.put("email", jwt.getClaimAsString("email"));
        profile.put("firstName", jwt.getClaimAsString("given_name"));
        profile.put("lastName", jwt.getClaimAsString("family_name"));
        profile.put("roles", authentication.getAuthorities());

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Map<String, Object>> getUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("tenantId", TenantContext.getCurrentTenant());
        response.put("message", "This is an admin endpoint");
        response.put("users", "List of users would be here");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/tenant/info")
    public ResponseEntity<Map<String, String>> getTenantInfo() {
        Map<String, String> response = new HashMap<>();
        response.put("currentTenant", TenantContext.getCurrentTenant());
        response.put("message", "Current tenant information");

        return ResponseEntity.ok(response);
    }
}
