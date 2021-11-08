package io.github.scru128;

import org.jetbrains.annotations.NotNull;

/**
 * Defines constants and convenience functions.
 */
public final class Scru128 {
    private Scru128() {
    }

    /**
     * Unix time in milliseconds at 2020-01-01 00:00:00+00:00.
     */
    public static final long TIMESTAMP_BIAS = 1577836800000L;

    /**
     * Maximum value of 28-bit counter field.
     */
    static final int MAX_COUNTER = 0xFFF_FFFF;

    /**
     * Maximum value of 24-bit per_sec_random field.
     */
    static final int MAX_PER_SEC_RANDOM = 0xFF_FFFF;

    /**
     * Maximum value of 32-bit per_sec_random field.
     */
    static final long MAX_PER_GEN_RANDOM = 0xFFFF_FFFFL;

    private static class DefaultGeneratorLazyHolder {
        static final @NotNull Scru128Generator DEFAULT_GENERATOR = new Scru128Generator();
    }

    /**
     * Generates a new SCRU128 ID encoded in a string.
     * <p>
     * Use this function to quickly get a new SCRU128 ID as a string. Use {@link Scru128Generator} to do more.
     * <p>
     * This function is thread safe in that it generates monotonically ordered IDs using a shared state when called
     * concurrently from multiple threads.
     *
     * @return 26-digit canonical string representation.
     */
    public static @NotNull String scru128() {
        return DefaultGeneratorLazyHolder.DEFAULT_GENERATOR.generate().toString();
    }
}
