package io.github.scru128;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class Scru128IdTests {
    static final long MAX_UINT44 = (1L << 44L) - 1L;
    static final int MAX_UINT28 = (1 << 28) - 1;
    static final int MAX_UINT24 = (1 << 24) - 1;
    static final long MAX_UINT32 = (1L << 32L) - 1L;

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

        ArrayList<Case> cases = new ArrayList<>();
        cases.add(new Case(0, 0, 0, 0, "00000000000000000000000000"));
        cases.add(new Case(MAX_UINT44, 0, 0, 0, "7VVVVVVVVG0000000000000000"));
        cases.add(new Case(MAX_UINT44, 0, 0, 0, "7vvvvvvvvg0000000000000000"));
        cases.add(new Case(0, MAX_UINT28, 0, 0, "000000000FVVVVU00000000000"));
        cases.add(new Case(0, MAX_UINT28, 0, 0, "000000000fvvvvu00000000000"));
        cases.add(new Case(0, 0, MAX_UINT24, 0, "000000000000001VVVVS000000"));
        cases.add(new Case(0, 0, MAX_UINT24, 0, "000000000000001vvvvs000000"));
        cases.add(new Case(0, 0, 0, MAX_UINT32, "00000000000000000003VVVVVV"));
        cases.add(new Case(0, 0, 0, MAX_UINT32, "00000000000000000003vvvvvv"));
        cases.add(new Case(MAX_UINT44, MAX_UINT28, MAX_UINT24, MAX_UINT32, "7VVVVVVVVVVVVVVVVVVVVVVVVV"));
        cases.add(new Case(MAX_UINT44, MAX_UINT28, MAX_UINT24, MAX_UINT32, "7vvvvvvvvvvvvvvvvvvvvvvvvv"));

        for (Case e : cases) {
            Scru128Id fromFields = Scru128Id.fromFields(e.timestamp, e.counter, e.perSecRandom, e.perGenRandom);
            Scru128Id fromString = Scru128Id.fromString(e.string);

            assertEquals(fromFields, fromString);
            assertEquals(new BigInteger(e.string, 32), new BigInteger(1, fromFields.toByteArray()));
            assertEquals(new BigInteger(e.string, 32), new BigInteger(1, fromString.toByteArray()));
            assertEquals(e.string.toUpperCase(), fromFields.toString());
            assertEquals(e.string.toUpperCase(), fromString.toString());
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
    @DisplayName("Throws error if an invalid string representation is supplied")
    void testStringValidation() {
        ArrayList<String> cases = new ArrayList<>();
        cases.add("");
        cases.add(" 00SCT4FL89GQPRHN44C4LFM0OV");
        cases.add("00SCT4FL89GQPRJN44C7SQO381 ");
        cases.add(" 00SCT4FL89GQPRLN44C4BGCIIO ");
        cases.add("+00SCT4FL89GQPRNN44C4F3QD24");
        cases.add("-00SCT4FL89GQPRPN44C7H4E5RC");
        cases.add("+0SCT4FL89GQPRRN44C55Q7RVC");
        cases.add("-0SCT4FL89GQPRTN44C6PN0A2R");
        cases.add("00SCT4FL89WQPRVN44C41RGVMM");
        cases.add("00SCT4FL89GQPS1N4_C54QDC5O");
        cases.add("00SCT4-L89GQPS3N44C602O0K8");
        cases.add("00SCT4FL89GQPS N44C7VHS5QJ");
        cases.add("80000000000000000000000000");
        cases.add("VVVVVVVVVVVVVVVVVVVVVVVVVV");

        for (String e : cases) {
            assertThrows(IllegalArgumentException.class, () -> {
                Scru128Id.fromString(e);
            });
        }
    }

    @Test
    @DisplayName("Has symmetric converters from/to various values")
    void testSymmetricConverters() throws IOException, ClassNotFoundException {
        ArrayList<Scru128Id> cases = new ArrayList<>();
        cases.add(Scru128Id.fromFields(0, 0, 0, 0));
        cases.add(Scru128Id.fromFields(MAX_UINT44, 0, 0, 0));
        cases.add(Scru128Id.fromFields(0, MAX_UINT28, 0, 0));
        cases.add(Scru128Id.fromFields(0, 0, MAX_UINT24, 0));
        cases.add(Scru128Id.fromFields(0, 0, 0, MAX_UINT32));
        cases.add(Scru128Id.fromFields(MAX_UINT44, MAX_UINT28, MAX_UINT24, MAX_UINT32));

        Scru128Generator g = new Scru128Generator();
        for (int i = 0; i < 1_000; i++) {
            cases.add(g.generate());
        }

        for (Scru128Id e : cases) {
            assertEquals(e, Scru128Id.fromString(e.toString()));
            assertEquals(e, Scru128Id.fromByteArray(e.toByteArray()));
            assertEquals(e, Scru128Id.fromFields(e.getTimestamp(), e.getCounter(), e.getPerSecRandom(),
                    e.getPerGenRandom()));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(e);
            oos.close();
            bos.close();
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            assertEquals(e, ois.readObject());
            ois.close();
            bis.close();
        }
    }

    @Test
    @DisplayName("Supports comparison methods")
    void testComparisonMethods() {
        ArrayList<Scru128Id> ordered = new ArrayList<>();
        ordered.add(Scru128Id.fromFields(0, 0, 0, 0));
        ordered.add(Scru128Id.fromFields(0, 0, 0, 1));
        ordered.add(Scru128Id.fromFields(0, 0, 0, MAX_UINT32));
        ordered.add(Scru128Id.fromFields(0, 0, 1, 0));
        ordered.add(Scru128Id.fromFields(0, 0, MAX_UINT24, 0));
        ordered.add(Scru128Id.fromFields(0, 1, 0, 0));
        ordered.add(Scru128Id.fromFields(0, MAX_UINT28, 0, 0));
        ordered.add(Scru128Id.fromFields(1, 0, 0, 0));
        ordered.add(Scru128Id.fromFields(2, 0, 0, 0));

        Scru128Generator g = new Scru128Generator();
        for (int i = 0; i < 1_000; i++) {
            ordered.add(g.generate());
        }

        Scru128Id prev = ordered.remove(0);
        for (Scru128Id curr : ordered) {
            assertFalse(curr.equals(prev));
            assertFalse(prev.equals(curr));
            assertNotEquals(curr.hashCode(), prev.hashCode());
            assertTrue(curr.compareTo(prev) > 0);
            assertTrue(prev.compareTo(curr) < 0);

            Scru128Id clone = Scru128Id.fromString(curr.toString());
            assertFalse(curr == clone);
            assertTrue(curr.equals(clone));
            assertTrue(clone.equals(curr));
            assertEquals(curr.hashCode(), clone.hashCode());
            assertEquals(curr.compareTo(clone), 0);
            assertEquals(clone.compareTo(curr), 0);

            prev = curr;
        }
    }
}
