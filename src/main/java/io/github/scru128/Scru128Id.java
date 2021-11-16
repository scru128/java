package io.github.scru128;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a SCRU128 ID and provides converters to/from String and 128-bit unsigned integer.
 * <p>
 * This class is designed to be an immutable data class, and thus the reference to an object can be safely copied and
 * shared by multiple variables without calling the clone() method.
 */
public final class Scru128Id implements Comparable<Scru128Id>, Serializable {
    private static final long serialVersionUID = 2;

    /**
     * Internal 128-bit byte array representation.
     */
    private final @NotNull byte[] bytes = new byte[16];

    /**
     * Creates an empty object.
     * <p>
     * This constructor is provided only for the convenience of some libraries that require parameterless constructors.
     */
    public Scru128Id() {
    }

    /**
     * Creates an object from a 128-bit unsigned integer.
     *
     * @param intValue 128-bit unsigned integer representation.
     * @return new object.
     * @throws IllegalArgumentException if the argument is out of the range of 128-bit unsigned integer.
     */
    public static @NotNull Scru128Id fromBigInteger(@NotNull BigInteger intValue) {
        Objects.requireNonNull(intValue);
        if (intValue.signum() < 0 || intValue.bitLength() > 128) {
            throw new IllegalArgumentException("not a 128-bit unsigned integer: " + intValue);
        }

        var object = new Scru128Id();
        var srcArray = intValue.toByteArray();
        if (srcArray.length > 16) {
            assert srcArray.length == 17 : "should be up to 129 bits including sign bit";
            System.arraycopy(srcArray, srcArray.length - 16, object.bytes, 0, 16);
        } else {
            System.arraycopy(srcArray, 0, object.bytes, 16 - srcArray.length, srcArray.length);
        }
        return object;
    }

    /**
     * Creates an object from field values.
     *
     * @param timestamp    44-bit millisecond timestamp field value.
     * @param counter      28-bit per-timestamp monotonic counter field value.
     * @param perSecRandom 24-bit per-second randomness field value.
     * @param perGenRandom 32-bit per-generation randomness field value.
     * @return new object.
     * @throws IllegalArgumentException if any argument is out of the range of each field.
     */
    public static @NotNull Scru128Id fromFields(long timestamp, int counter, int perSecRandom, long perGenRandom) {
        if (timestamp < 0 ||
                counter < 0 ||
                perSecRandom < 0 ||
                perGenRandom < 0 ||
                timestamp > 0xFFF_FFFF_FFFFL ||
                counter > Scru128.MAX_COUNTER ||
                perSecRandom > Scru128.MAX_PER_SEC_RANDOM ||
                perGenRandom > 0xFFFF_FFFFL) {
            throw new IllegalArgumentException("invalid field value");
        }

        var object = new Scru128Id();
        object.bytes[0] = (byte) (timestamp >>> 36);
        object.bytes[1] = (byte) (timestamp >>> 28);
        object.bytes[2] = (byte) (timestamp >>> 20);
        object.bytes[3] = (byte) (timestamp >> 12);
        object.bytes[4] = (byte) (timestamp >>> 4);
        object.bytes[5] = (byte) (timestamp << 4 | counter >>> 24);
        object.bytes[6] = (byte) (counter >>> 16);
        object.bytes[7] = (byte) (counter >>> 8);
        object.bytes[8] = (byte) counter;
        object.bytes[9] = (byte) (perSecRandom >>> 16);
        object.bytes[10] = (byte) (perSecRandom >>> 8);
        object.bytes[11] = (byte) perSecRandom;
        object.bytes[12] = (byte) (perGenRandom >>> 24);
        object.bytes[13] = (byte) (perGenRandom >>> 16);
        object.bytes[14] = (byte) (perGenRandom >>> 8);
        object.bytes[15] = (byte) perGenRandom;
        return object;
    }

    private static class ValidPatternLazyHolder {
        static final @NotNull Pattern VALID_PATTERN = Pattern.compile("[0-7][0-9A-Va-v]{25}");
    }

    /**
     * Creates an object from a 26-digit string representation.
     *
     * @param strValue 26-digit string representation.
     * @return new object.
     * @throws IllegalArgumentException if the argument is not a valid string representation.
     */
    public static @NotNull Scru128Id fromString(@NotNull String strValue) {
        Objects.requireNonNull(strValue);
        if (!ValidPatternLazyHolder.VALID_PATTERN.matcher(strValue).matches()) {
            throw new IllegalArgumentException("invalid string representation: " + strValue);
        }

        var object = new Scru128Id();
        long buffer = Long.parseLong(strValue.substring(0, 2), 32);
        assert buffer <= 0xFF : "should be no greater than `7V`";
        object.bytes[0] = (byte) buffer;

        // process three 40-bit (5-byte / 8-digit) groups
        for (var i = 0; i < 3; i++) {
            buffer = Long.parseLong(strValue.substring(2 + i * 8, 10 + i * 8), 32);
            for (var j = 0; j < 5; j++) {
                object.bytes[5 + i * 5 - j] = (byte) buffer;
                buffer >>>= 8;
            }
        }
        return object;
    }

    /**
     * Returns the 128-bit unsigned integer representation.
     *
     * @return 128-bit unsigned integer representation.
     */
    public @NotNull BigInteger toBigInteger() {
        return new BigInteger(1, bytes);
    }

    /**
     * Returns the 44-bit millisecond timestamp field value.
     *
     * @return 44-bit unsigned integer.
     */
    public long getTimestamp() {
        return subLong(0, 6) >>> 4;
    }

    /**
     * Returns the 28-bit per-timestamp monotonic counter field value.
     *
     * @return 28-bit unsigned integer.
     */
    public int getCounter() {
        return (int) (subLong(5, 9) & Scru128.MAX_COUNTER);
    }

    /**
     * Returns the 24-bit per-second randomness field value.
     *
     * @return 24-bit unsigned integer.
     */
    public int getPerSecRandom() {
        return (int) subLong(9, 12);
    }

    /**
     * Returns the 32-bit per-generation randomness field value.
     *
     * @return 32-bit unsigned integer.
     */
    public long getPerGenRandom() {
        return subLong(12, 16);
    }

    private static final char[] DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUV".toCharArray();

    /**
     * Returns the 26-digit canonical string representation.
     */
    @Override
    public @NotNull String toString() {
        var chars = new char[26];
        chars[0] = DIGITS[Byte.toUnsignedInt(bytes[0]) >>> 5];
        chars[1] = DIGITS[bytes[0] & 31];

        // process three 40-bit (5-byte / 8-digit) groups
        for (var i = 0; i < 3; i++) {
            long buffer = subLong(i * 5, i * 5 + 6);
            for (var j = 0; j < 8; j++) {
                chars[9 + i * 8 - j] = DIGITS[(int) (buffer & 31)];
                buffer >>>= 5;
            }
        }
        return String.valueOf(chars);
    }

    /**
     * Returns true if the object is equivalent to the argument.
     */
    @Override
    public boolean equals(@Nullable Object other) {
        return (other instanceof Scru128Id) && Arrays.equals(bytes, ((Scru128Id) other).bytes);
    }

    /**
     * Returns a hash code value for the object.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    /**
     * Returns a negative integer, zero, and positive integer if the object is less than, equal to, and greater than
     * the argument, respectively.
     */
    @Override
    public int compareTo(@NotNull Scru128Id other) {
        Objects.requireNonNull(other);
        return Arrays.compareUnsigned(bytes, other.bytes);
    }

    /**
     * Returns a part of {@code bytes} as an unsigned long value.
     *
     * @return long value representing {@code bytes} from {@code beginIndex} to {@code endIndex - 1}, inclusive.
     */
    private long subLong(int beginIndex, int endIndex) {
        long buffer = 0;
        while (beginIndex < endIndex) {
            buffer <<= 8;
            buffer |= Byte.toUnsignedLong(bytes[beginIndex++]);
        }
        return buffer;
    }
}
