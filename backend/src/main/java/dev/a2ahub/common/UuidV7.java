package dev.a2ahub.common;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Pure Java 21 RFC 9562 UUIDv7 generator.
 * Produces monotonic time-ordered UUIDs to prevent B-Tree index fragmentation in PostgreSQL.
 */
public final class UuidV7 {

    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidV7() {}

    public static UUID generate() {
        long timestamp = System.currentTimeMillis();
        // 48-bit timestamp ms in the highest bits + 4-bit version 7 (0x7000) + 12-bit entropy
        long mostSigBits = (timestamp << 16) | 0x7000L | (RANDOM.nextLong() & 0x0FFFL);
        // 2-bit RFC 4122/9562 variant (0x8000...) + 62-bit entropy
        long leastSigBits = 0x8000000000000000L | (RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL);
        return new UUID(mostSigBits, leastSigBits);
    }
}
