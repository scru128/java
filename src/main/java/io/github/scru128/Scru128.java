package io.github.scru128;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private static class DefaultGeneratorLazyHolder {
        static final @NotNull Scru128Generator DEFAULT_GENERATOR = new Scru128Generator();
    }

    /**
     * Generates a new SCRU128 ID object.
     * <p>
     * This function is thread safe; multiple threads can call it concurrently.
     *
     * @return new object.
     */
    public static @NotNull Scru128Id generate() {
        return DefaultGeneratorLazyHolder.DEFAULT_GENERATOR.generate();
    }

    /**
     * Generates a new SCRU128 ID encoded in a string.
     * <p>
     * This function is thread safe. Use this to quickly get a new SCRU128 ID as a string.
     *
     * @return 26-digit canonical string representation.
     */
    public static @NotNull String generateString() {
        return generate().toString();
    }

    /**
     * Defines the logger interface used in the package.
     */
    public interface Logger {
        /**
         * Logs message at ERROR level.
         *
         * @param message message.
         */
        void error(@NotNull String message);

        /**
         * Logs message at WARNING level.
         *
         * @param message message.
         */
        void warn(@NotNull String message);

        /**
         * Logs message at INFO level.
         *
         * @param message message.
         */
        void info(@NotNull String message);
    }

    /**
     * Logger object used in the package.
     */
    static @Nullable Logger logger = null;

    /**
     * Specifies the logger object used in the package.
     * <p>
     * Logging is disabled by default. Set a thread-safe logger to enable logging.
     *
     * @param logger new logger.
     */
    public static void setLogger(@NotNull Logger logger) {
        Scru128.logger = logger;
    }
}
