package io.github.scru128;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Scru128GeneratorGenerateCoreTests {
    @Test
    @DisplayName("Generates increasing IDs even with decreasing or constant timestamp")
    void testDecreasingOrConstantTimestamp() {
        long ts = 0x0123_4567_89abL;
        Scru128Generator g = new Scru128Generator();
        assertEquals(g.getLastStatus(), Scru128Generator.Status.NOT_EXECUTED);

        Scru128Id prev = g.generateCore(ts, 10000);
        assertEquals(g.getLastStatus(), Scru128Generator.Status.NEW_TIMESTAMP);
        assertEquals(prev.getTimestamp(), ts);

        for (long i = 0; i < 100_000; i++) {
            Scru128Id curr = g.generateCore(ts - Math.min(9_998, i), 10000);
            assertTrue(g.getLastStatus() == Scru128Generator.Status.COUNTER_LO_INC ||
                    g.getLastStatus() == Scru128Generator.Status.COUNTER_HI_INC ||
                    g.getLastStatus() == Scru128Generator.Status.TIMESTAMP_INC);
            assertTrue(prev.compareTo(curr) < 0);
            prev = curr;
        }
        assertTrue(prev.getTimestamp() >= ts);
    }

    @Test
    @DisplayName("Breaks increasing order of IDs if timestamp moves backward a lot")
    void testTimestampRollback() {
        long ts = 0x0123_4567_89abL;
        Scru128Generator g = new Scru128Generator();
        assertEquals(g.getLastStatus(), Scru128Generator.Status.NOT_EXECUTED);

        Scru128Id prev = g.generateCore(ts, 10000);
        assertEquals(g.getLastStatus(), Scru128Generator.Status.NEW_TIMESTAMP);
        assertEquals(prev.getTimestamp(), ts);

        Scru128Id curr = g.generateCore(ts - 10_000, 10000);
        assertEquals(g.getLastStatus(), Scru128Generator.Status.CLOCK_ROLLBACK);
        assertTrue(prev.compareTo(curr) > 0);
        assertEquals(curr.getTimestamp(), ts - 10_000);

        prev = curr;
        curr = g.generateCore(ts - 10_001, 10000);
        assertTrue(g.getLastStatus() == Scru128Generator.Status.COUNTER_LO_INC ||
                g.getLastStatus() == Scru128Generator.Status.COUNTER_HI_INC ||
                g.getLastStatus() == Scru128Generator.Status.TIMESTAMP_INC);
        assertTrue(prev.compareTo(curr) < 0);
    }
}

class Scru128GeneratorGenerateCoreNoRewindTests {
    @Test
    @DisplayName("Generates increasing IDs even with decreasing or constant timestamp")
    void testDecreasingOrConstantTimestamp() {
        long ts = 0x0123_4567_89abL;
        Scru128Generator g = new Scru128Generator();
        assertEquals(g.getLastStatus(), Scru128Generator.Status.NOT_EXECUTED);

        Scru128Id prev = g.generateCoreNoRewind(ts, 10000);
        assertNotNull(prev);
        assertEquals(g.getLastStatus(), Scru128Generator.Status.NEW_TIMESTAMP);
        assertEquals(prev.getTimestamp(), ts);

        for (long i = 0; i < 100_000; i++) {
            Scru128Id curr = g.generateCoreNoRewind(ts - Math.min(9_998, i), 10000);
            assertNotNull(curr);
            assertTrue(g.getLastStatus() == Scru128Generator.Status.COUNTER_LO_INC ||
                    g.getLastStatus() == Scru128Generator.Status.COUNTER_HI_INC ||
                    g.getLastStatus() == Scru128Generator.Status.TIMESTAMP_INC);
            assertTrue(prev.compareTo(curr) < 0);
            prev = curr;
        }
        assertTrue(prev.getTimestamp() >= ts);
    }

    @Test
    @DisplayName("Returns null if timestamp moves backward a lot")
    void testTimestampRollback() {
        long ts = 0x0123_4567_89abL;
        Scru128Generator g = new Scru128Generator();
        assertEquals(g.getLastStatus(), Scru128Generator.Status.NOT_EXECUTED);

        Scru128Id prev = g.generateCoreNoRewind(ts, 10000);
        assertNotNull(prev);
        assertEquals(g.getLastStatus(), Scru128Generator.Status.NEW_TIMESTAMP);
        assertEquals(prev.getTimestamp(), ts);

        Scru128Id curr = g.generateCoreNoRewind(ts - 10_000, 10000);
        assertNull(curr);
        assertEquals(g.getLastStatus(), Scru128Generator.Status.NEW_TIMESTAMP);

        curr = g.generateCoreNoRewind(ts - 10_001, 10000);
        assertNull(curr);
        assertEquals(g.getLastStatus(), Scru128Generator.Status.NEW_TIMESTAMP);
    }
}

class Scru128GeneratorTests {
    @Test
    @DisplayName("Is iterable with for-each loop")
    void testIterableImplementation() {
        int i = 0;
        for (Scru128Id e : new Scru128Generator()) {
            assert (e.getTimestamp() > 0);
            i += 1;
            if (i > 100) {
                break;
            }
        }
        assertEquals(101, i);
    }
}
