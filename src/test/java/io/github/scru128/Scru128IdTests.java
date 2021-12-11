package io.github.scru128;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class Scru128IdTests {
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
        cases.add(new Case((long) Math.pow(2, 44) - 1, 0, 0, 0, "7VVVVVVVVG0000000000000000"));
        cases.add(new Case((long) Math.pow(2, 44) - 1, 0, 0, 0, "7vvvvvvvvg0000000000000000"));
        cases.add(new Case(0, (int) Math.pow(2, 28) - 1, 0, 0, "000000000FVVVVU00000000000"));
        cases.add(new Case(0, (int) Math.pow(2, 28) - 1, 0, 0, "000000000fvvvvu00000000000"));
        cases.add(new Case(0, 0, (int) Math.pow(2, 24) - 1, 0, "000000000000001VVVVS000000"));
        cases.add(new Case(0, 0, (int) Math.pow(2, 24) - 1, 0, "000000000000001vvvvs000000"));
        cases.add(new Case(0, 0, 0, (long) Math.pow(2, 32) - 1, "00000000000000000003VVVVVV"));
        cases.add(new Case(0, 0, 0, (long) Math.pow(2, 32) - 1, "00000000000000000003vvvvvv"));
        cases.add(new Case((long) Math.pow(2, 44) - 1, (int) Math.pow(2, 28) - 1, (int) Math.pow(2, 24) - 1,
                (long) Math.pow(2, 32) - 1, "7VVVVVVVVVVVVVVVVVVVVVVVVV"));
        cases.add(new Case((long) Math.pow(2, 44) - 1, (int) Math.pow(2, 28) - 1, (int) Math.pow(2, 24) - 1,
                (long) Math.pow(2, 32) - 1, "7vvvvvvvvvvvvvvvvvvvvvvvvv"));

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
    @DisplayName("Has symmetric converters from/to String, byte array, fields, and serialized form")
    void testSymmetricConverters() throws IOException, ClassNotFoundException {
        Scru128Generator g = new Scru128Generator();
        for (int i = 0; i < 1_000; i++) {
            Scru128Id obj = g.generate();
            assertEquals(obj, Scru128Id.fromString(obj.toString()));
            assertEquals(obj, Scru128Id.fromByteArray(obj.toByteArray()));
            assertEquals(obj, Scru128Id.fromFields(obj.getTimestamp(), obj.getCounter(), obj.getPerSecRandom(),
                    obj.getPerGenRandom()));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.close();
            bos.close();
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            assertEquals(obj, ois.readObject());
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
        ordered.add(Scru128Id.fromFields(0, 0, 0, 0xFFFF_FFFFL));
        ordered.add(Scru128Id.fromFields(0, 0, 1, 0));
        ordered.add(Scru128Id.fromFields(0, 0, 0xFF_FFFF, 0));
        ordered.add(Scru128Id.fromFields(0, 1, 0, 0));
        ordered.add(Scru128Id.fromFields(0, 0xFFF_FFFF, 0, 0));
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
