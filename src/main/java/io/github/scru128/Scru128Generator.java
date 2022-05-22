package io.github.scru128;

import org.jetbrains.annotations.NotNull;

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
     * Status code reported at the last generation.
     */
    private @NotNull Status lastStatus = Status.NOT_EXECUTED;

    /**
     * Random number generator used by the generator.
     */
    private final @NotNull Random random;

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
     * This method is thread-safe; multiple threads can call it concurrently.
     *
     * @return new SCRU128 ID object.
     */
    public synchronized @NotNull Scru128Id generate() {
        return generateCore(System.currentTimeMillis());
    }

    /**
     * Generates a new SCRU128 ID object with the timestamp passed.
     * <p>
     * Unlike {@link #generate()}, this method is NOT thread-safe. The generator object should be protected from
     * concurrent accesses using a mutex or other synchronization mechanism to avoid race conditions.
     *
     * @param timestamp 48-bit timestamp field value.
     * @return new SCRU128 ID object.
     * @throws IllegalArgumentException if the argument is not a 48-bit positive integer.
     */
    public @NotNull Scru128Id generateCore(long timestamp) {
        if (timestamp < 1 || timestamp > Scru128.MAX_TIMESTAMP) {
            throw new IllegalArgumentException("`timestamp` must be a 48-bit positive integer");
        }

        lastStatus = Status.NEW_TIMESTAMP;
        if (timestamp > this.timestamp) {
            this.timestamp = timestamp;
            counterLo = random.nextInt() & Scru128.MAX_COUNTER_LO;
        } else if (timestamp + 10000 > this.timestamp) {
            counterLo++;
            lastStatus = Status.COUNTER_LO_INC;
            if (counterLo > Scru128.MAX_COUNTER_LO) {
                counterLo = 0;
                counterHi++;
                lastStatus = Status.COUNTER_HI_INC;
                if (counterHi > Scru128.MAX_COUNTER_HI) {
                    counterHi = 0;
                    // increment timestamp at counter overflow
                    this.timestamp++;
                    counterLo = random.nextInt() & Scru128.MAX_COUNTER_LO;
                    lastStatus = Status.TIMESTAMP_INC;
                }
            }
        } else {
            // reset state if clock moves back by ten seconds or more
            tsCounterHi = 0;
            this.timestamp = timestamp;
            counterLo = random.nextInt() & Scru128.MAX_COUNTER_LO;
            lastStatus = Status.CLOCK_ROLLBACK;
        }

        if (this.timestamp - tsCounterHi >= 1000) {
            tsCounterHi = this.timestamp;
            counterHi = random.nextInt() & Scru128.MAX_COUNTER_HI;
        }

        return Scru128Id.fromFields(this.timestamp, counterHi, counterLo, 0xffff_ffffL & random.nextInt());
    }

    /**
     * Returns a {@link Status} code that indicates the internal state involved in the last generation of ID.
     * <p>
     * Note that the generator object should be protected from concurrent accesses during the sequential calls to a
     * generation method and this method to avoid race conditions.
     *
     * @return status code from the last generation of ID.
     */
    public @NotNull Status getLastStatus() {
        return lastStatus;
    }

    /**
     * Status code returned by {@link #getLastStatus()} method.
     */
    public enum Status {
        /**
         * Indicates that the generator has yet to generate an ID.
         */
        NOT_EXECUTED,

        /**
         * Indicates that the latest timestamp was used because it was greater than the previous one.
         */
        NEW_TIMESTAMP,

        /**
         * Indicates that counter_lo was incremented because the latest timestamp was no greater than the previous one.
         */
        COUNTER_LO_INC,

        /**
         * Indicates that counter_hi was incremented because counter_lo reached its maximum value.
         */
        COUNTER_HI_INC,

        /**
         * Indicates that the previous timestamp was incremented because counter_hi reached its maximum value.
         */
        TIMESTAMP_INC,

        /**
         * Indicates that the monotonic order of generated IDs was broken because the latest timestamp was less than
         * the previous one by ten seconds or more.
         */
        CLOCK_ROLLBACK,
    }
}
