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
        for (var i = 0; i < 100_000; i++) {
            SAMPLES.add(Scru128.scru128());
        }
    }

    @Test
    @DisplayName("Generates 26-digit canonical string")
    void testFormat() {
        var pattern = Pattern.compile("^[0-7][0-9A-V]{25}$");
        for (var e : SAMPLES) {
            assertTrue(pattern.matcher(e).matches());
        }
    }

    @Test
    @DisplayName("Generates 100k identifiers without collision")
    void testUniqueness() {
        var set = new HashSet<>(SAMPLES);
        assertEquals(SAMPLES.size(), set.size());
    }

    @Test
    @DisplayName("Generates sortable string representation by creation time")
    void testOrder() {
        for (var i = 1; i < SAMPLES.size(); i++) {
            assertTrue(SAMPLES.get(i - 1).compareTo(SAMPLES.get(i)) < 0);
        }
    }

    @Test
    @DisplayName("Encodes up-to-date timestamp")
    void testTimestamp() {
        var g = new Scru128Generator();
        for (var i = 0; i < 10_000; i++) {
            var tsNow = System.currentTimeMillis() - 1577836800_000L;
            var timestamp = g.generate().getTimestamp();
            assertTrue(Math.abs(tsNow - timestamp) < 16);
        }
    }

    @Test
    @DisplayName("Encodes unique sortable pair of timestamp and counter")
    void testTimestampAndCounter() {
        var prev = Scru128Id.fromString(SAMPLES.get(0));
        for (var i = 1; i < SAMPLES.size(); i++) {
            var curr = Scru128Id.fromString(SAMPLES.get(i));
            assertTrue(prev.getTimestamp() < curr.getTimestamp() ||
                    (prev.getTimestamp() == curr.getTimestamp() &&
                            prev.getCounter() < curr.getCounter()));
            prev = curr;
        }
    }

    @Test
    @DisplayName("Generates no IDs sharing same timestamp and counter under multithreading")
    void testThreading() throws InterruptedException {
        var queue = new ArrayList<String>();
        var producers = new ArrayList<Thread>();
        for (var i = 0; i < 4; i++) {
            var thread = new Thread(() -> {
                for (var j = 0; j < 10_000; j++) {
                    var x = Scru128.scru128();
                    synchronized (queue) {
                        queue.add(x);
                    }
                }
            });
            thread.start();
            producers.add(thread);
        }

        for (var e : producers) {
            e.join();
        }

        var set = new HashSet<String>();
        for (var e : queue) {
            var x = Scru128Id.fromString(e);
            set.add(String.format("%011x-%07x", x.getTimestamp(), x.getCounter()));
        }

        assertEquals(4 * 10_000, set.size());
    }
}
