package io.github.scru128;

import org.jetbrains.annotations.NotNull;

/**
 * Defines constants and convenience functions.
 */
public final class Scru128 {
    private Scru128() {
    }

    /**
     * The maximum value of 48-bit timestamp field.
     */
    static final long MAX_TIMESTAMP = 0xffff_ffff_ffffL;

    /**
     * The maximum value of 24-bit counter_hi field.
     */
    static final int MAX_COUNTER_HI = 0xff_ffff;

    /**
     * The maximum value of 24-bit counter_lo field.
     */
    static final int MAX_COUNTER_LO = 0xff_ffff;

    private static class GlobalGeneratorLazyHolder {
        static final @NotNull Scru128Generator GLOBAL_GENERATOR = new Scru128Generator();
    }

    /**
     * Generates a new SCRU128 ID object using the global generator.
     * <p>
     * This function is thread-safe; multiple threads can call it concurrently.
     *
     * @return A new object.
     */
    public static @NotNull Scru128Id generate() {
        return GlobalGeneratorLazyHolder.GLOBAL_GENERATOR.generate();
    }

    /**
     * Generates a new SCRU128 ID encoded in a string using the global generator.
     * <p>
     * This function is thread-safe. Use this to quickly get a new SCRU128 ID as a string.
     *
     * @return A 25-digit canonical string representation.
     */
    public static @NotNull String generateString() {
        return generate().toString();
    }
}
