package com.schoolSys.schooolSys.multitenancy;

import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TenantContext Unit Tests")
class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Nested
    @DisplayName("getCurrentTenant()")
    class GetCurrentTenant {

        @Test
        @DisplayName("should return 'public' when no tenant is set")
        void shouldReturnPublicByDefault() {
            assertThat(TenantContext.getCurrentTenant()).isEqualTo("public");
        }

        @Test
        @DisplayName("should return the tenant set for this thread")
        void shouldReturnSetTenant() {
            TenantContext.setCurrentTenant("school_abc");
            assertThat(TenantContext.getCurrentTenant()).isEqualTo("school_abc");
        }

        @Test
        @DisplayName("should return 'public' after clear()")
        void shouldReturnPublicAfterClear() {
            TenantContext.setCurrentTenant("school_abc");
            TenantContext.clear();
            assertThat(TenantContext.getCurrentTenant()).isEqualTo("public");
        }
    }

    @Nested
    @DisplayName("Thread isolation")
    class ThreadIsolation {

        @Test
        @DisplayName("tenant should be isolated per thread")
        void shouldIsolatePerThread() throws InterruptedException {
            TenantContext.setCurrentTenant("school_main");

            AtomicReference<String> childTenant = new AtomicReference<>();
            Thread child = new Thread(() -> {
                // Child thread should see default, not parent's tenant
                childTenant.set(TenantContext.getCurrentTenant());
            });
            child.start();
            child.join();

            assertThat(TenantContext.getCurrentTenant()).isEqualTo("school_main");
            assertThat(childTenant.get()).isEqualTo("public");
        }
    }
}
