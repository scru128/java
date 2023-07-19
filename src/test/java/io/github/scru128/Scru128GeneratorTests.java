package io.github.scru128;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Scru128GeneratorGenerateOrResetTests {
    @Test
    @DisplayName("Generates increasing IDs even with decreasing or constant timestamp")
    void testDecreasingOrConstantTimestamp() {
        long ts = 0x0123_4567_89abL;
        Scru128Generator g = new Scru128Generator();

        Scru128Id prev = g.generateOrResetCore(ts, 10_000);
        assertEquals(prev.getTimestamp(), ts);

        for (long i = 0; i < 100_000; i++) {
            Scru128Id curr = g.generateOrResetCore(ts - Math.min(9_999, i), 10_000);
            assertTrue(prev.compareTo(curr) < 0);
            prev = curr;
        }
        assertTrue(prev.getTimestamp() >= ts);
    }

    @Test
    @DisplayName("Breaks increasing order of IDs if timestamp goes backwards a lot")
    void testTimestampRollback() {
        long ts = 0x0123_4567_89abL;
        Scru128Generator g = new Scru128Generator();

        Scru128Id prev = g.generateOrResetCore(ts, 10_000);
        assertEquals(prev.getTimestamp(), ts);

        Scru128Id curr = g.generateOrResetCore(ts - 10_000, 10_000);
        assertTrue(prev.compareTo(curr) < 0);

        prev = curr;
        curr = g.generateOrResetCore(ts - 10_001, 10_000);
        assertTrue(prev.compareTo(curr) > 0);
        assertEquals(curr.getTimestamp(), ts - 10_001);

        prev = curr;
        curr = g.generateOrResetCore(ts - 10_002, 10_000);
        assertTrue(prev.compareTo(curr) < 0);
    }
}

class Scru128GeneratorGenerateOrAbortTests {
    @Test
    @DisplayName("Generates increasing IDs even with decreasing or constant timestamp")
    void testDecreasingOrConstantTimestamp() {
        long ts = 0x0123_4567_89abL;
        Scru128Generator g = new Scru128Generator();

        Scru128Id prev = g.generateOrAbortCore(ts, 10000);
        assertNotNull(prev);
        assertEquals(prev.getTimestamp(), ts);

        for (long i = 0; i < 100_000; i++) {
            Scru128Id curr = g.generateOrAbortCore(ts - Math.min(9_999, i), 10000);
            assertNotNull(curr);
            assertTrue(prev.compareTo(curr) < 0);
            prev = curr;
        }
        assertTrue(prev.getTimestamp() >= ts);
    }

    @Test
    @DisplayName("Returns null if timestamp goes backwards a lot")
    void testTimestampRollback() {
        long ts = 0x0123_4567_89abL;
        Scru128Generator g = new Scru128Generator();

        Scru128Id prev = g.generateOrAbortCore(ts, 10000);
        assertNotNull(prev);
        assertEquals(prev.getTimestamp(), ts);

        Scru128Id curr = g.generateOrAbortCore(ts - 10_000, 10000);
        assertTrue(prev.compareTo(curr) < 0);

        curr = g.generateOrAbortCore(ts - 10_001, 10000);
        assertNull(curr);

        curr = g.generateOrAbortCore(ts - 10_002, 10000);
        assertNull(curr);
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
