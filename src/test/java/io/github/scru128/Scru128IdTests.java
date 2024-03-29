package io.github.scru128;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class Scru128IdTests {
    static final long MAX_UINT48 = (1L << 48L) - 1L;
    static final int MAX_UINT24 = (1 << 24) - 1;
    static final long MAX_UINT32 = (1L << 32L) - 1L;

    @Test
    @DisplayName("Encodes and decodes prepared cases correctly")
    void testEncodeDecode() {
        class Case {
            final long timestamp;
            final int counterHi;
            final int counterLo;
            final long entropy;
            final String string;

            Case(long timestamp, int counterHi, int counterLo, long entropy, String string) {
                this.timestamp = timestamp;
                this.counterHi = counterHi;
                this.counterLo = counterLo;
                this.entropy = entropy;
                this.string = string;
            }
        }

        ArrayList<Case> cases = new ArrayList<>();
        cases.add(new Case(0, 0, 0, 0, "0000000000000000000000000"));
        cases.add(new Case(MAX_UINT48, 0, 0, 0, "F5LXX1ZZ5K6TP71GEEH2DB7K0"));
        cases.add(new Case(MAX_UINT48, 0, 0, 0, "f5lxx1zz5k6tp71geeh2db7k0"));
        cases.add(new Case(0, MAX_UINT24, 0, 0, "0000000005GV2R2KJWR7N8XS0"));
        cases.add(new Case(0, MAX_UINT24, 0, 0, "0000000005gv2r2kjwr7n8xs0"));
        cases.add(new Case(0, 0, MAX_UINT24, 0, "00000000000000JPIA7QL4HS0"));
        cases.add(new Case(0, 0, MAX_UINT24, 0, "00000000000000jpia7ql4hs0"));
        cases.add(new Case(0, 0, 0, MAX_UINT32, "0000000000000000001Z141Z3"));
        cases.add(new Case(0, 0, 0, MAX_UINT32, "0000000000000000001z141z3"));
        cases.add(new Case(MAX_UINT48, MAX_UINT24, MAX_UINT24, MAX_UINT32, "F5LXX1ZZ5PNORYNQGLHZMSP33"));
        cases.add(new Case(MAX_UINT48, MAX_UINT24, MAX_UINT24, MAX_UINT32, "f5lxx1zz5pnorynqglhzmsp33"));

        for (Case e : cases) {
            Scru128Id fromFields = Scru128Id.fromFields(e.timestamp, e.counterHi, e.counterLo, e.entropy);
            Scru128Id fromString = Scru128Id.fromString(e.string);

            assertEquals(fromFields, fromString);
            assertEquals(new BigInteger(e.string, 36), new BigInteger(1, fromFields.toByteArray()));
            assertEquals(new BigInteger(e.string, 36), new BigInteger(1, fromString.toByteArray()));
            assertEquals(e.string.toLowerCase(), fromFields.toString());
            assertEquals(e.string.toLowerCase(), fromString.toString());
            assertEquals(e.timestamp, fromFields.getTimestamp());
            assertEquals(e.timestamp, fromString.getTimestamp());
            assertEquals(e.counterHi, fromFields.getCounterHi());
            assertEquals(e.counterHi, fromString.getCounterHi());
            assertEquals(e.counterLo, fromFields.getCounterLo());
            assertEquals(e.counterLo, fromString.getCounterLo());
            assertEquals(e.entropy, fromFields.getEntropy());
            assertEquals(e.entropy, fromString.getEntropy());
        }
    }

    @Test
    @DisplayName("Throws error if an invalid string representation is supplied")
    void testStringValidation() {
        ArrayList<String> cases = new ArrayList<>();
        cases.add("");
        cases.add(" 036z8puq4tsxsigk6o19y164q");
        cases.add("036z8puq54qny1vq3hcbrkweb ");
        cases.add(" 036z8puq54qny1vq3helivwax ");
        cases.add("+036z8puq54qny1vq3hfcv3ss0");
        cases.add("-036z8puq54qny1vq3hhy8u1ch");
        cases.add("+36z8puq54qny1vq3hjq48d9p");
        cases.add("-36z8puq5a7j0ti08oz6zdrdy");
        cases.add("036z8puq5a7j0t_08p2cdz28v");
        cases.add("036z8pu-5a7j0ti08p3ol8ool");
        cases.add("036z8puq5a7j0ti08p4j 6cya");
        cases.add("f5lxx1zz5pnorynqglhzmsp34");
        cases.add("zzzzzzzzzzzzzzzzzzzzzzzzz");
        cases.add("039o\tvvklfmqlqe7fzllz7c7t");
        cases.add("039onvvklfmqlq漢字fgvd1");
        cases.add("039onvvkl🤣qe7fzr2hdoqu");
        cases.add("頭onvvklfmqlqe7fzrhtgcfz");
        cases.add("039onvvklfmqlqe7fztft5尾");
        cases.add("039漢字a52xp4bvf4sn94e09cja");
        cases.add("039ooa52xp4bv😘sn97642mwl");

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
        cases.add(Scru128Id.fromFields(MAX_UINT48, 0, 0, 0));
        cases.add(Scru128Id.fromFields(0, MAX_UINT24, 0, 0));
        cases.add(Scru128Id.fromFields(0, 0, MAX_UINT24, 0));
        cases.add(Scru128Id.fromFields(0, 0, 0, MAX_UINT32));
        cases.add(Scru128Id.fromFields(MAX_UINT48, MAX_UINT24, MAX_UINT24, MAX_UINT32));

        Scru128Generator g = new Scru128Generator();
        for (int i = 0; i < 1_000; i++) {
            cases.add(g.generate());
        }

        for (Scru128Id e : cases) {
            assertEquals(e, Scru128Id.fromString(e.toString()));
            assertEquals(e, Scru128Id.fromByteArray(e.toByteArray()));
            assertEquals(e, Scru128Id.fromFields(e.getTimestamp(), e.getCounterHi(), e.getCounterLo(), e.getEntropy()));

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
        ordered.add(Scru128Id.fromFields(0, MAX_UINT24, 0, 0));
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
