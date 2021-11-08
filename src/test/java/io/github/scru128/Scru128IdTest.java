package io.github.scru128;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class Scru128IdTest {
    @Test
    @DisplayName("Encodes and decodes prepared cases correctly")
    void testEncodeDecode() {
        class Case {
            final long timestamp;
            final int counter;
            final int perSecRandom;
            final long perGenRandom;
            final String string;

            Case(long timestamp, int counter, int perSecRandom, long perGenRandom, String string) {
                this.timestamp = timestamp;
                this.counter = counter;
                this.perSecRandom = perSecRandom;
                this.perGenRandom = perGenRandom;
                this.string = string;
            }
        }

        var cases = new ArrayList<Case>();
        cases.add(new Case(0, 0, 0, 0, "00000000000000000000000000"));
        cases.add(new Case((long) Math.pow(2, 44) - 1, 0, 0, 0, "7VVVVVVVVG0000000000000000"));
        cases.add(new Case(0, (int) Math.pow(2, 28) - 1, 0, 0, "000000000FVVVVU00000000000"));
        cases.add(new Case(0, 0, (int) Math.pow(2, 24) - 1, 0, "000000000000001VVVVS000000"));
        cases.add(new Case(0, 0, 0, (long) Math.pow(2, 32) - 1, "00000000000000000003VVVVVV"));
        cases.add(new Case((long) Math.pow(2, 44) - 1, (int) Math.pow(2, 28) - 1, (int) Math.pow(2, 24) - 1,
                (long) Math.pow(2, 32) - 1, "7VVVVVVVVVVVVVVVVVVVVVVVVV"));

        for (var e : cases) {
            var fromFields = Scru128Id.fromFields(e.timestamp, e.counter, e.perSecRandom, e.perGenRandom);
            var fromString = Scru128Id.fromString(e.string);

            assertEquals(fromFields, fromString);
            assertEquals(new BigInteger(e.string, 32), fromFields.toBigInteger());
            assertEquals(new BigInteger(e.string, 32), fromString.toBigInteger());
            assertEquals(e.string, fromFields.toString());
            assertEquals(e.string, fromString.toString());
            assertEquals(e.timestamp, fromFields.getTimestamp());
            assertEquals(e.timestamp, fromString.getTimestamp());
            assertEquals(e.counter, fromFields.getCounter());
            assertEquals(e.counter, fromString.getCounter());
            assertEquals(e.perSecRandom, fromFields.getPerSecRandom());
            assertEquals(e.perSecRandom, fromString.getPerSecRandom());
            assertEquals(e.perGenRandom, fromFields.getPerGenRandom());
            assertEquals(e.perGenRandom, fromString.getPerGenRandom());
        }
    }

    @Test
    @DisplayName("Has symmetric converters from/to String, BigInteger, and fields")
    void testSymmetricConverters() {
        var g = new Scru128Generator();
        for (var i = 0; i < 1_000; i++) {
            var obj = g.generate();
            assertEquals(obj, Scru128Id.fromString(obj.toString()));
            assertEquals(obj, Scru128Id.fromBigInteger(obj.toBigInteger()));
            assertEquals(obj, Scru128Id.fromFields(obj.getTimestamp(), obj.getCounter(), obj.getPerSecRandom(),
                    obj.getPerGenRandom()));
        }
    }

    @Test
    @DisplayName("Supports comparison methods")
    void testComparisonMethods() {
        var ordered = new ArrayList<Scru128Id>();
        ordered.add(Scru128Id.fromFields(0, 0, 0, 0));
        ordered.add(Scru128Id.fromFields(0, 0, 0, 1));
        ordered.add(Scru128Id.fromFields(0, 0, 1, 0));
        ordered.add(Scru128Id.fromFields(0, 1, 0, 0));
        ordered.add(Scru128Id.fromFields(1, 0, 0, 0));

        var g = new Scru128Generator();
        for (var i = 0; i < 1_000; i++) {
            ordered.add(g.generate());
        }

        var prev = ordered.remove(0);
        for (var curr : ordered) {
            assertFalse(curr.equals(prev));
            assertFalse(prev.equals(curr));
            assertTrue(curr.compareTo(prev) > 0);
            assertTrue(prev.compareTo(curr) < 0);

            assertTrue(curr.equals(curr));
            assertEquals(curr.compareTo(curr), 0);

            prev = curr;
        }
    }
}
