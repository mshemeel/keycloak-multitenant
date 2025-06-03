package com.shemeel.muhammed.keycloakmultitenant.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    private static final String TEST_TENANT = "tenant1";

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldSetAndGetCurrentTenant() {
        // When
        TenantContext.setCurrentTenant(TEST_TENANT);

        // Then
        assertEquals(TEST_TENANT, TenantContext.getCurrentTenant());
    }

    @Test
    void shouldReturnNullWhenNoTenantSet() {
        // When/Then
        assertNull(TenantContext.getCurrentTenant());
    }

    @Test
    void shouldClearTenant() {
        // Given
        TenantContext.setCurrentTenant(TEST_TENANT);

        // When
        TenantContext.clear();

        // Then
        assertNull(TenantContext.getCurrentTenant());
    }

    @Test
    void shouldIsolateTenantsBetweenThreads() throws InterruptedException {
        // Given
        TenantContext.setCurrentTenant(TEST_TENANT);
        CountDownLatch latch = new CountDownLatch(1);
        String[] threadTenant = new String[1];

        // When
        Thread thread = new Thread(() -> {
            threadTenant[0] = TenantContext.getCurrentTenant();
            latch.countDown();
        });
        thread.start();
        latch.await(1, TimeUnit.SECONDS);

        // Then
        assertEquals(TEST_TENANT, TenantContext.getCurrentTenant());
        assertNull(threadTenant[0]);
    }

    @Test
    void shouldHandleMultipleThreads() throws InterruptedException {
        // Given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        String[] tenants = new String[threadCount];

        // When
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    String tenant = "tenant" + index;
                    TenantContext.setCurrentTenant(tenant);
                    tenants[index] = TenantContext.getCurrentTenant();
                } finally {
                    TenantContext.clear();
                    latch.countDown();
                }
            });
        }
        latch.await(2, TimeUnit.SECONDS);
        executorService.shutdown();

        // Then
        for (int i = 0; i < threadCount; i++) {
            assertEquals("tenant" + i, tenants[i]);
        }
    }
} 