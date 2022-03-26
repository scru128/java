package io.github.scru128;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Represents a SCRU128 ID generator that encapsulates the monotonic counters and other internal states.
 */
public class Scru128Generator {
    private long timestamp = 0;
    private int counterHi = 0;
    private int counterLo = 0;

    /**
     * Timestamp at the last renewal of counter_hi field.
     */
    private long tsCounterHi = 0;

    /**
     * Random number generator used by the generator.
     */
    private final @NotNull Random random;

    /**
     * Logger object used by the generator.
     */
    private @Nullable Scru128Generator.Logger logger;

    /**
     * Creates a generator object with the default random number generator.
     */
    public Scru128Generator() {
        this(new SecureRandom());
    }

    /**
     * Creates a generator object with a specified random number generator.
     *
     * @param random random number generator (should be cryptographically strong and securely seeded).
     */
    public Scru128Generator(@NotNull Random random) {
        this.random = Objects.requireNonNull(random);
    }

    /**
     * Generates a new SCRU128 ID object.
     * <p>
     * This method is thread safe; multiple threads can call it concurrently.
     *
     * @return new SCRU128 ID object.
     */
    public synchronized @NotNull Scru128Id generate() {
        return generateThreadUnsafe();
    }

    /**
     * Generates a new SCRU128 ID object without overhead for thread safety.
     */
    private @NotNull Scru128Id generateThreadUnsafe() {
        long ts = System.currentTimeMillis();
        if (ts > timestamp) {
            timestamp = ts;
            counterLo = random.nextInt() & Scru128.MAX_COUNTER_LO;
            if (ts - tsCounterHi >= 1000) {
                tsCounterHi = ts;
                counterHi = random.nextInt() & Scru128.MAX_COUNTER_HI;
            }
        } else {
            counterLo++;
            if (counterLo > Scru128.MAX_COUNTER_LO) {
                counterLo = 0;
                counterHi++;
                if (counterHi > Scru128.MAX_COUNTER_HI) {
                    counterHi = 0;
                    handleCounterOverflow();
                    return generateThreadUnsafe();
                }
            }
        }

        return Scru128Id.fromFields(timestamp, counterHi, counterLo, 0xffff_ffffL & random.nextInt());
    }

    /**
     * Defines the behavior on counter overflow.
     * <p>
     * Currently, this method busy-waits for the next clock tick and, if the clock does not move forward for a while,
     * reinitializes the generator state.
     */
    private void handleCounterOverflow() {
        if (logger != null) {
            logger.warn("counter overflowing; will wait for next clock tick");
        }
        tsCounterHi = 0;
        for (int i = 0; i < 1_000_000; i++) {
            if (System.currentTimeMillis() > timestamp) {
                return;
            }
        }
        if (logger != null) {
            logger.warn("reset state as clock did not move for a while");
        }
        timestamp = 0;
    }


    /**
     * Defines the logger interface used by the generator.
     */
    public interface Logger {
        /**
         * Logs message at WARNING level.
         *
         * @param message message.
         */
        void warn(@NotNull String message);
    }

    /**
     * Specifies the logger object used by the generator.
     * <p>
     * Logging is disabled by default. Set a logger object to enable logging.
     *
     * @param logger new logger.
     */
    public void setLogger(@NotNull Scru128Generator.Logger logger) {
        this.logger = Objects.requireNonNull(logger);
    }
}
