package net.liosk.scru128;

import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Represents a SCRU128 ID generator and provides an interface to do more than just generate a string representation.
 */
public class Scru128Generator {
    /**
     * Timestamp at last generation.
     */
    private long tsLastGen = 0;

    /**
     * Counter at last generation.
     */
    private int counter = 0;

    /**
     * Timestamp at last renewal of perSecRandom.
     */
    private long tsLastSec = 0;

    /**
     * Per-second random value at last generation.
     */
    private int perSecRandom = 0;

    /**
     * Maximum number of checking System.currentTimeMillis() until clock goes forward.
     */
    private int nClockCheckMax = 1_000_000;

    private @NotNull Random random = new SecureRandom();

    /**
     * Generates a new SCRU128 ID object.
     *
     * @return new SCRU128 ID object.
     */
    public synchronized @NotNull Scru128Id generate() {
        return generate_thread_unsafe();
    }

    /**
     * Generates a new SCRU128 ID object without overhead for thread safety.
     */
    private @NotNull Scru128Id generate_thread_unsafe() {
        var tsNow = System.currentTimeMillis();

        // update timestamp and counter
        if (tsNow > tsLastGen) {
            tsLastGen = tsNow;
            counter = random.nextInt() & Scru128.MAX_COUNTER;
        } else if (++counter > Scru128.MAX_COUNTER) {
            var logger = System.getLogger("net.liosk.scru128");
            logger.log(System.Logger.Level.INFO, "counter limit reached; will wait until clock goes forward");
            var nClockCheck = 0;
            while (tsNow <= tsLastGen) {
                tsNow = System.currentTimeMillis();
                if (++nClockCheck > nClockCheckMax) {
                    logger.log(System.Logger.Level.WARNING, "reset state as clock did not go forward");
                    tsLastSec = 0;
                    break;
                }
            }

            tsLastGen = tsNow;
            counter = random.nextInt() & Scru128.MAX_COUNTER;
        }

        // update perSecRandom
        if (tsLastGen - tsLastSec > 1_000) {
            tsLastSec = tsLastGen;
            perSecRandom = random.nextInt() & Scru128.MAX_PER_SEC_RANDOM;
        }

        return Scru128Id.fromFields(
                tsNow - Scru128.TIMESTAMP_BIAS,
                counter,
                perSecRandom,
                Integer.toUnsignedLong(random.nextInt())
        );
    }
}
