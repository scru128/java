package io.github.scru128;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Scru128GeneratorTests {
    @Test
    @DisplayName("Generates increasing IDs even with decreasing or constant timestamp")
    void testDecreasingOrConstantTimestamp() {
        long ts = 0x0123_4567_89abL;
        Scru128Generator g = new Scru128Generator();
        Scru128Generator.Result prev = g.generateCore(ts);
        assertEquals(prev.status, Scru128Generator.Status.NEW_TIMESTAMP);
        assertEquals(prev.value.getTimestamp(), ts);
        for (long i = 0; i < 100_000; i++) {
            Scru128Generator.Result curr = g.generateCore(ts - Math.min(9_998, i));
            assertTrue(curr.status == Scru128Generator.Status.COUNTER_LO_INC ||
                    curr.status == Scru128Generator.Status.COUNTER_HI_INC ||
                    curr.status == Scru128Generator.Status.TIMESTAMP_INC);
            assertTrue(prev.value.compareTo(curr.value) < 0);
            prev = curr;
        }
        assertTrue(prev.value.getTimestamp() >= ts);
    }

    @Test
    @DisplayName("Breaks increasing order of IDs if timestamp moves backward a lot")
    void testTimestampRollback() {
        long ts = 0x0123_4567_89abL;
        Scru128Generator g = new Scru128Generator();
        Scru128Generator.Result prev = g.generateCore(ts);
        assertEquals(prev.status, Scru128Generator.Status.NEW_TIMESTAMP);
        assertEquals(prev.value.getTimestamp(), ts);
        Scru128Generator.Result curr = g.generateCore(ts - 10_000);
        assertEquals(curr.status, Scru128Generator.Status.CLOCK_ROLLBACK);
        assertTrue(prev.value.compareTo(curr.value) > 0);
        assertEquals(curr.value.getTimestamp(), ts - 10_000);
    }
}
