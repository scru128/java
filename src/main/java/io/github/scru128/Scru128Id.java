package io.github.scru128;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a SCRU128 ID and provides converters to/from String and 128-bit unsigned integer.
 * <p>
 * This class is designed to be an immutable data class, and thus the reference to an object can be safely copied and
 * shared by multiple variables without calling the clone() method.
 */
public final class Scru128Id implements Comparable<Scru128Id>, Serializable {
    private static final long serialVersionUID = 1;

    /**
     * Internal 128-bit unsigned integer representation.
     */
    private final @NotNull BigInteger value;

    /**
     * Creates an object from a 128-bit unsigned integer.
     * <p>
     * The primary constructor is concealed so the internal BigInteger implementation can be superseded in the future.
     */
    private Scru128Id(@NotNull BigInteger intValue) {
        value = Objects.requireNonNull(intValue);
        if (value.signum() < 0 || value.bitLength() > 128) {
            throw new IllegalArgumentException("not a 128-bit unsigned integer: " + value);
        }
    }

    /**
     * Creates an empty object.
     * <p>
     * This constructor is provided only for the convenience of some libraries that require parameterless constructors.
     */
    public Scru128Id() {
        this(BigInteger.ZERO);
    }

    /**
     * Creates an object from a 128-bit unsigned integer.
     *
     * @param intValue 128-bit unsigned integer representation.
     * @return new object.
     * @throws IllegalArgumentException if the argument is out of the range of 128-bit unsigned integer.
     */
    public static @NotNull Scru128Id fromBigInteger(@NotNull BigInteger intValue) {
        return new Scru128Id(intValue);
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
                perGenRandom > Scru128.MAX_PER_GEN_RANDOM) {
            throw new IllegalArgumentException("invalid field value");
        }

        return new Scru128Id(new BigInteger(1, new byte[]{
                (byte) (timestamp >> 36),
                (byte) (timestamp >> 28),
                (byte) (timestamp >> 20),
                (byte) (timestamp >> 12),
                (byte) (timestamp >> 4),
                (byte) ((timestamp << 4) | (counter >> 24)),
                (byte) (counter >> 16),
                (byte) (counter >> 8),
                (byte) counter,
                (byte) (perSecRandom >> 16),
                (byte) (perSecRandom >> 8),
                (byte) perSecRandom,
                (byte) (perGenRandom >> 24),
                (byte) (perGenRandom >> 16),
                (byte) (perGenRandom >> 8),
                (byte) perGenRandom,
        }));
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
        return new Scru128Id(new BigInteger(strValue, 32));
    }

    /**
     * Returns the 128-bit unsigned integer representation.
     *
     * @return 128-bit unsigned integer representation.
     */
    public @NotNull BigInteger toBigInteger() {
        return value;
    }

    /**
     * Returns the 44-bit millisecond timestamp field value.
     *
     * @return 44-bit unsigned integer.
     */
    public long getTimestamp() {
        return value.shiftRight(84).longValue();
    }

    /**
     * Returns the 28-bit per-timestamp monotonic counter field value.
     *
     * @return 28-bit unsigned integer.
     */
    public int getCounter() {
        return value.shiftRight(56).intValue() & Scru128.MAX_COUNTER;
    }

    /**
     * Returns the 24-bit per-second randomness field value.
     *
     * @return 24-bit unsigned integer.
     */
    public int getPerSecRandom() {
        return value.shiftRight(32).intValue() & Scru128.MAX_PER_SEC_RANDOM;
    }

    /**
     * Returns the 32-bit per-generation randomness field value.
     *
     * @return 32-bit unsigned integer.
     */
    public long getPerGenRandom() {
        return value.longValue() & Scru128.MAX_PER_GEN_RANDOM;
    }

    /**
     * Returns the 26-digit canonical string representation.
     */
    @Override
    public @NotNull String toString() {
        var ds = value.toString(32).toUpperCase();
        return "00000000000000000000000000".substring(ds.length()) + ds;
    }

    /**
     * Returns true if the object is equivalent to the argument.
     */
    @Override
    public boolean equals(@Nullable Object other) {
        return (other instanceof Scru128Id) && value.equals(((Scru128Id) other).value);
    }

    /**
     * Returns a hash code value for the object.
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Returns -1, 0, and 1 if the object is less than, equal to, and greater than the argument, respectively.
     */
    @Override
    public int compareTo(@NotNull Scru128Id other) {
        return value.compareTo(other.value);
    }
}
