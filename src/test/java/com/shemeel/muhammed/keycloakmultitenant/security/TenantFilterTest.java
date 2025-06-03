package com.shemeel.muhammed.keycloakmultitenant.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantFilterTest {

    private TenantFilter tenantFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String TEST_TENANT = "tenant1";

    @BeforeEach
    void setUp() {
        tenantFilter = new TenantFilter();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldSetTenantContextWhenHeaderPresent() throws ServletException, IOException {
        // Given
        when(request.getHeader(TENANT_HEADER)).thenReturn(TEST_TENANT);
        doAnswer(invocation -> {
            assertEquals(TEST_TENANT, TenantContext.getCurrentTenant());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        tenantFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(TenantContext.getCurrentTenant()); // Should be cleared after filter
    }

    @Test
    void shouldNotSetTenantContextWhenHeaderMissing() throws ServletException, IOException {
        // Given
        when(request.getHeader(TENANT_HEADER)).thenReturn(null);
        doAnswer(invocation -> {
            assertNull(TenantContext.getCurrentTenant());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        tenantFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(TenantContext.getCurrentTenant());
    }

    @Test
    void shouldNotSetTenantContextWhenHeaderEmpty() throws ServletException, IOException {
        // Given
        when(request.getHeader(TENANT_HEADER)).thenReturn("");
        doAnswer(invocation -> {
            assertNull(TenantContext.getCurrentTenant());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        tenantFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(TenantContext.getCurrentTenant());
    }

    @Test
    void shouldClearTenantContextAfterFiltering() throws ServletException, IOException {
        // Given
        when(request.getHeader(TENANT_HEADER)).thenReturn(TEST_TENANT);
        doAnswer(invocation -> {
            assertEquals(TEST_TENANT, TenantContext.getCurrentTenant());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        tenantFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(TenantContext.getCurrentTenant());
    }

    @Test
    void shouldClearTenantContextEvenWhenFilterChainThrowsException() throws ServletException, IOException {
        // Given
        when(request.getHeader(TENANT_HEADER)).thenReturn(TEST_TENANT);
        doAnswer(invocation -> {
            assertEquals(TEST_TENANT, TenantContext.getCurrentTenant());
            throw new RuntimeException("Test exception");
        }).when(filterChain).doFilter(request, response);

        // When/Then
        assertThrows(RuntimeException.class, () ->
                tenantFilter.doFilterInternal(request, response, filterChain));
        assertNull(TenantContext.getCurrentTenant());
    }
} 