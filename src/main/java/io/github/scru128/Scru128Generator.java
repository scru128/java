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
        } else if (ts + 10000 > timestamp) {
            counterLo++;
            if (counterLo > Scru128.MAX_COUNTER_LO) {
                counterLo = 0;
                counterHi++;
                if (counterHi > Scru128.MAX_COUNTER_HI) {
                    counterHi = 0;
                    // increment timestamp at counter overflow
                    timestamp++;
                    counterLo = random.nextInt() & Scru128.MAX_COUNTER_LO;
                }
            }
        } else {
            // reset state if clock moves back more than ten seconds
            tsCounterHi = 0;
            timestamp = ts;
            counterLo = random.nextInt() & Scru128.MAX_COUNTER_LO;
        }

        if (timestamp - tsCounterHi >= 1000) {
            tsCounterHi = timestamp;
            counterHi = random.nextInt() & Scru128.MAX_COUNTER_HI;
        }

        return Scru128Id.fromFields(timestamp, counterHi, counterLo, 0xffff_ffffL & random.nextInt());
    }
}
