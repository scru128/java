package io.github.scru128;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;

/**
 * Represents a SCRU128 ID generator that encapsulates the monotonic counters and other internal states.
 * <p>
 * The generator offers four different methods to generate a SCRU128 ID:
 * <table border="1">
 *   <caption>Comparison of generator functions</caption>
 *   <tr><th>Flavor</th>                           <th>Timestamp</th><th>Thread-</th><th>On big clock rewind</th></tr>
 *   <tr><td>{@link #generate}</td>                <td>Now</td>      <td>Safe</td>   <td>Rewinds state</td></tr>
 *   <tr><td>{@link #generateNoRewind}</td>        <td>Now</td>      <td>Safe</td>   <td>Returns null</td></tr>
 *   <tr><td>{@link #generateCore(long, long)}</td><td>Argument</td> <td>Unsafe</td> <td>Rewinds state</td></tr>
 *   <tr><td>{@link #generateCoreNoRewind}</td>    <td>Argument</td> <td>Unsafe</td> <td>Returns null</td></tr>
 * </table>
 * <p>
 * Each method returns monotonically increasing IDs unless a timestamp provided is significantly (by ten seconds or
 * more by default) smaller than the one embedded in the immediately preceding ID. If such a significant clock
 * rollback is detected, the standard {@link #generate} rewinds the generator state and returns a new ID based on the
 * current timestamp, whereas NoRewind variants keep the state untouched and return null. Core functions offer
 * low-level thread-unsafe primitives.
 */
public class Scru128Generator implements Iterable<@NotNull Scru128Id>, Iterator<@NotNull Scru128Id> {
    /**
     * The default timestamp rollback allowance.
     */
    static final long DEFAULT_ROLLBACK_ALLOWANCE = 10000L; // 10 seconds

    private long timestamp = 0;
    private int counterHi = 0;
    private int counterLo = 0;

    /**
     * The timestamp at the last renewal of counter_hi field.
     */
    private long tsCounterHi = 0;

    /**
     * The status code reported at the last generation.
     */
    private @NotNull Status lastStatus = Status.NOT_EXECUTED;

    /**
     * The random number generator used by the generator.
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
     * @param random A random number generator (which should be cryptographically strong and securely seeded).
     */
    public Scru128Generator(@NotNull Random random) {
        this.random = Objects.requireNonNull(random);
    }

    /**
     * Generates a new SCRU128 ID object from the current timestamp.
     * <p>
     * See the {@link Scru128Generator} class documentation for the description.
     *
     * @return A new SCRU128 ID object.
     */
    public synchronized @NotNull Scru128Id generate() {
        return generateCore(System.currentTimeMillis(), DEFAULT_ROLLBACK_ALLOWANCE);
    }

    /**
     * Generates a new SCRU128 ID object from the current timestamp, guaranteeing the monotonic order of generated IDs
     * despite a significant timestamp rollback.
     * <p>
     * See the {@link Scru128Generator} class documentation for the description.
     *
     * @return A new SCRU128 ID object.
     */
    public synchronized @Nullable Scru128Id generateNoRewind() {
        return generateCoreNoRewind(System.currentTimeMillis(), DEFAULT_ROLLBACK_ALLOWANCE);
    }

    /**
     * Generates a new SCRU128 ID object from the timestamp passed.
     * <p>
     * See the {@link Scru128Generator} class documentation for the description.
     * <p>
     * Unlike {@link #generate}, this method is NOT thread-safe. The generator object should be protected from
     * concurrent accesses using a mutex or other synchronization mechanism to avoid race conditions.
     *
     * @param timestamp         A 48-bit timestamp field value.
     * @param rollbackAllowance The amount of timestamp rollback that is considered significant. A suggested value is
     *                          {@code 10_000} (milliseconds).
     * @return A new SCRU128 ID object.
     * @throws IllegalArgumentException if the timestamp is not a 48-bit positive integer.
     */
    public @NotNull Scru128Id generateCore(long timestamp, long rollbackAllowance) {
        Scru128Id value = generateCoreNoRewind(timestamp, rollbackAllowance);
        if (value == null) {
            // reset state and resume
            this.timestamp = 0;
            tsCounterHi = 0;
            value = generateCoreNoRewind(timestamp, rollbackAllowance);
            lastStatus = Status.CLOCK_ROLLBACK;
            assert value != null;
        }
        return value;
    }

    /**
     * Generates a new SCRU128 ID object from the timestamp passed.
     *
     * @param timestamp A 48-bit timestamp field value.
     * @return A new SCRU128 ID object.
     * @deprecated Use {@link #generateCore(long, long)}.
     */
    @Deprecated
    public @NotNull Scru128Id generateCore(long timestamp) {
        return generateCore(timestamp, DEFAULT_ROLLBACK_ALLOWANCE);
    }

    /**
     * Generates a new SCRU128 ID object from the timestamp passed, guaranteeing the monotonic order of generated IDs
     * despite a significant timestamp rollback.
     * <p>
     * See the {@link Scru128Generator} class documentation for the description.
     * <p>
     * Unlike {@link #generateNoRewind}, this method is NOT thread-safe. The generator object should be protected from
     * concurrent accesses using a mutex or other synchronization mechanism to avoid race conditions.
     *
     * @param timestamp         A 48-bit timestamp field value.
     * @param rollbackAllowance The amount of timestamp rollback that is considered significant. A suggested value is
     *                          {@code 10_000} (milliseconds).
     * @return A new SCRU128 ID object.
     * @throws IllegalArgumentException if the timestamp is not a 48-bit positive integer.
     */
    public @Nullable Scru128Id generateCoreNoRewind(long timestamp, long rollbackAllowance) {
        if (timestamp < 1 || timestamp > Scru128.MAX_TIMESTAMP) {
            throw new IllegalArgumentException("`timestamp` must be a 48-bit positive integer");
        } else if (rollbackAllowance < 0 || rollbackAllowance > Scru128.MAX_TIMESTAMP) {
            throw new IllegalArgumentException("`rollbackAllowance` out of reasonable range");
        }

        lastStatus = Status.NEW_TIMESTAMP;
        if (timestamp > this.timestamp) {
            this.timestamp = timestamp;
            counterLo = random.nextInt() & Scru128.MAX_COUNTER_LO;
        } else if (timestamp + rollbackAllowance > this.timestamp) {
            // go on with previous timestamp if new one is not much smaller
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
            // abort if clock moves back to unbearable extent
            return null;
        }

        if (this.timestamp - tsCounterHi >= 1000 || tsCounterHi < 1) {
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
     * @return A status code from the last generation of ID.
     * @deprecated Use {@link #generateNoRewind} to guarantee monotonicity.
     */
    @Deprecated
    public @NotNull Status getLastStatus() {
        return lastStatus;
    }

    /**
     * Returns an infinite iterator object that produces a new ID for each call of {@code next()}.
     *
     * @return An infinite iterator.
     */
    @Override
    public @NotNull Iterator<@NotNull Scru128Id> iterator() {
        return this;
    }

    /**
     * Returns {@code true} always for {@code this} to behave as an infinite iterator.
     *
     * @return {@code true} always.
     */
    @Override
    public boolean hasNext() {
        return true;
    }

    /**
     * Returns a new SCRU128 ID object for each call, infinitely.
     * <p>
     * This method is a synonym for {@link #generate} to use {@code this} as an infinite iterator.
     *
     * @return A new SCRU128 ID object.
     */
    @Override
    public @NotNull Scru128Id next() {
        return generate();
    }

    /**
     * The status code returned by {@link #getLastStatus} method.
     *
     * @deprecated Use {@link #generateNoRewind} to guarantee monotonicity.
     */
    @Deprecated
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
