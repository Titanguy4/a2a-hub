package dev.a2ahub.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UuidV7 Generator Unit Tests")
class UuidV7Test {

    @Test
    @DisplayName("Should generate RFC 9562 compliant UUID with version 7 and variant 2")
    void shouldComplyWithRfc9562() {
        UUID uuid = UuidV7.generate();

        assertThat(uuid).isNotNull();
        assertThat(uuid.version()).isEqualTo(7);
        assertThat(uuid.variant()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should encode current millisecond timestamp in the highest 48 bits")
    void shouldEncodeTimestampAccurately() {
        long before = System.currentTimeMillis();
        UUID uuid = UuidV7.generate();
        long after = System.currentTimeMillis();

        long timestamp = uuid.getMostSignificantBits() >>> 16;
        assertThat(timestamp).isBetween(before, after);
    }

    @Test
    @DisplayName("Should preserve monotonic time-ordering across successive calls")
    void shouldPreserveMonotonicTimeOrdering() {
        long start = System.currentTimeMillis();
        UUID u1 = UuidV7.generate();
        while (System.currentTimeMillis() <= start) {
            Thread.onSpinWait();
        }
        UUID u2 = UuidV7.generate();

        assertThat(u1.compareTo(u2)).isLessThan(0);
    }

    @Test
    @DisplayName("Should guarantee collision-free uniqueness under high concurrency across Virtual Threads")
    void shouldBeCollisionFreeUnderVirtualThreadConcurrency() throws Exception {
        int count = 10_000;
        Set<UUID> generated = ConcurrentHashMap.newKeySet(count);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                futures.add(executor.submit(() -> generated.add(UuidV7.generate())));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        }

        assertThat(generated).hasSize(count);
    }
}
