package io.github.scru128;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Scru128Tests {
    static final ArrayList<String> SAMPLES = new ArrayList<>();

    @BeforeAll
    static void setup() {
        for (int i = 0; i < 100_000; i++) {
            SAMPLES.add(Scru128.generateString());
        }
    }

    @Test
    @DisplayName("Generates 26-digit canonical string")
    void testFormat() {
        Pattern pattern = Pattern.compile("^[0-7][0-9A-V]{25}$");
        for (String e : SAMPLES) {
            assertTrue(pattern.matcher(e).matches());
        }
    }

    @Test
    @DisplayName("Generates 100k identifiers without collision")
    void testUniqueness() {
        HashSet<String> set = new HashSet<>(SAMPLES);
        assertEquals(SAMPLES.size(), set.size());
    }

    @Test
    @DisplayName("Generates sortable string representation by creation time")
    void testOrder() {
        for (int i = 1; i < SAMPLES.size(); i++) {
            assertTrue(SAMPLES.get(i - 1).compareTo(SAMPLES.get(i)) < 0);
        }
    }

    @Test
    @DisplayName("Encodes up-to-date timestamp")
    void testTimestamp() {
        Scru128Generator g = new Scru128Generator();
        for (int i = 0; i < 10_000; i++) {
            long tsNow = System.currentTimeMillis() - 1577836800_000L;
            long timestamp = g.generate().getTimestamp();
            assertTrue(Math.abs(tsNow - timestamp) < 16);
        }
    }

    @Test
    @DisplayName("Encodes unique sortable pair of timestamp and counter")
    void testTimestampAndCounter() {
        Scru128Id prev = Scru128Id.fromString(SAMPLES.get(0));
        for (int i = 1; i < SAMPLES.size(); i++) {
            Scru128Id curr = Scru128Id.fromString(SAMPLES.get(i));
            assertTrue(prev.getTimestamp() < curr.getTimestamp() ||
                    (prev.getTimestamp() == curr.getTimestamp() &&
                            prev.getCounter() < curr.getCounter()));
            prev = curr;
        }
    }

    @Test
    @DisplayName("Generates no IDs sharing same timestamp and counter under multithreading")
    void testThreading() throws InterruptedException {
        ArrayList<Scru128Id> queue = new ArrayList<>();
        ArrayList<Thread> producers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 10_000; j++) {
                    Scru128Id x = Scru128.generate();
                    synchronized (queue) {
                        queue.add(x);
                    }
                }
            });
            thread.start();
            producers.add(thread);
        }

        for (Thread e : producers) {
            e.join();
        }

        HashSet<String> set = new HashSet<>();
        for (Scru128Id e : queue) {
            set.add(String.format("%011x-%07x", e.getTimestamp(), e.getCounter()));
        }

        assertEquals(4 * 10_000, set.size());
    }
}
