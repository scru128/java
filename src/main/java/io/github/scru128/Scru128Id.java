package io.github.scru128;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a SCRU128 ID and provides converters and comparison operators.
 * <p>
 * This class is designed to be an immutable data class, and thus the reference to an object can be safely copied and
 * shared by multiple variables without calling the clone() method.
 */
public final class Scru128Id implements Comparable<@NotNull Scru128Id>, Serializable {
    private static final long serialVersionUID = 4;

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
     * Creates an object from a byte array that represents a 128-bit unsigned integer.
     *
     * @param byteArray any byte array that represents a 128-bit unsigned integer in the big-endian (network) byte
     *                  order. The byte array can be shorter than 16 bytes (128 bits) and in that case the missing
     *                  significant bytes are all assumed to be zero, or it can be longer than 16 bytes as long as
     *                  the extra significant bytes are all zero.
     * @return A new object.
     * @throws IllegalArgumentException if the argument byte array does not fit in 128 bits.
     */
    public static @NotNull Scru128Id fromByteArray(@NotNull byte[] byteArray) {
        Objects.requireNonNull(byteArray);
        Scru128Id object = new Scru128Id();
        if (byteArray.length <= 16) {
            System.arraycopy(byteArray, 0, object.bytes, 16 - byteArray.length, byteArray.length);
        } else {
            for (int i = 0; i < byteArray.length - 16; i++) {
                if (byteArray[i] != 0) {
                    throw new IllegalArgumentException("could not interpret byte array as 128-bit unsigned integer");
                }
            }
            System.arraycopy(byteArray, byteArray.length - 16, object.bytes, 0, 16);
        }
        return object;
    }

    /**
     * Creates an object from field values.
     *
     * @param timestamp A 48-bit timestamp field value.
     * @param counterHi A 24-bit counter_hi field value.
     * @param counterLo A 24-bit counter_lo field value.
     * @param entropy   A 32-bit entropy field value.
     * @return A new object.
     * @throws IllegalArgumentException if any argument is out of the value range of the field.
     */
    public static @NotNull Scru128Id fromFields(long timestamp, int counterHi, int counterLo, long entropy) {
        if (timestamp < 0 ||
                counterHi < 0 ||
                counterLo < 0 ||
                entropy < 0 ||
                timestamp > Scru128.MAX_TIMESTAMP ||
                counterHi > Scru128.MAX_COUNTER_HI ||
                counterLo > Scru128.MAX_COUNTER_LO ||
                entropy > 0xffff_ffffL) {
            throw new IllegalArgumentException("invalid field value");
        }

        Scru128Id object = new Scru128Id();
        object.bytes[0] = (byte) (timestamp >>> 40);
        object.bytes[1] = (byte) (timestamp >>> 32);
        object.bytes[2] = (byte) (timestamp >>> 24);
        object.bytes[3] = (byte) (timestamp >>> 16);
        object.bytes[4] = (byte) (timestamp >>> 8);
        object.bytes[5] = (byte) timestamp;
        object.bytes[6] = (byte) (counterHi >>> 16);
        object.bytes[7] = (byte) (counterHi >>> 8);
        object.bytes[8] = (byte) counterHi;
        object.bytes[9] = (byte) (counterLo >>> 16);
        object.bytes[10] = (byte) (counterLo >>> 8);
        object.bytes[11] = (byte) counterLo;
        object.bytes[12] = (byte) (entropy >>> 24);
        object.bytes[13] = (byte) (entropy >>> 16);
        object.bytes[14] = (byte) (entropy >>> 8);
        object.bytes[15] = (byte) entropy;
        return object;
    }

    /**
     * An O(1) map from ASCII code points to Base36 digit values.
     */
    private static final @NotNull byte[] DECODE_MAP = new byte[]{
            0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
            0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
            0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x00, 0x01, 0x02,
            0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x0a, 0x0b, 0x0c,
            0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d,
            0x1e, 0x1f, 0x20, 0x21, 0x22, 0x23, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e,
            0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f,
            0x20, 0x21, 0x22, 0x23, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
    };

    /**
     * Creates an object from a 25-digit string representation.
     *
     * @param strValue A 25-digit string representation.
     * @return A new object.
     * @throws IllegalArgumentException if the argument is not a valid string representation.
     */
    public static @NotNull Scru128Id fromString(@NotNull String strValue) {
        Objects.requireNonNull(strValue);
        if (strValue.length() != 25) {
            throw new IllegalArgumentException(String.format("invalid length: %d (expected 25)", strValue.length()));
        }

        byte[] src = new byte[25];
        for (int i = 0; i < 25; i++) {
            char c = strValue.charAt(i);
            if (c > 'z' || DECODE_MAP[c] == 0x7f) {
                String s = new String(new int[]{strValue.codePointAt(i)}, 0, 1);
                throw new IllegalArgumentException(String.format("invalid digit '%s' at %d", s, i));
            }
            src[i] = DECODE_MAP[c];
        }

        Scru128Id dst = new Scru128Id();
        int minIndex = 99; // any number greater than size of output array
        for (int i = -5; i < 25; i += 10) {
            // implement Base36 using 10-digit words
            long carry = 0;
            for (int j = i < 0 ? 0 : i; j < i + 10; j++) {
                carry = (carry * 36) + src[j];
            }

            // iterate over output array from right to left while carry != 0 but at least up to place already filled
            int j = dst.bytes.length - 1;
            for (; carry > 0 || j > minIndex; j--) {
                if (j < 0) {
                    throw new IllegalArgumentException("out of 128-bit value range");
                }
                carry += (0xffL & dst.bytes[j]) * 3656158440062976L; // 36^10
                dst.bytes[j] = (byte) carry;
                carry = carry >>> 8;
            }
            minIndex = j;
        }
        return dst;
    }

    /**
     * Returns a byte array containing the 128-bit unsigned integer representation.
     *
     * @return A 16-byte byte array containing the 128-bit unsigned integer representation in the big-endian (network)
     * byte order.
     */
    public @NotNull byte[] toByteArray() {
        return Arrays.copyOf(bytes, 16);
    }

    /**
     * Returns the 48-bit timestamp field value.
     *
     * @return A 48-bit unsigned integer.
     */
    public long getTimestamp() {
        return subLong(0, 6);
    }

    /**
     * Returns the 24-bit counter_hi field value.
     *
     * @return A 24-bit unsigned integer.
     */
    public int getCounterHi() {
        return (int) subLong(6, 9);
    }

    /**
     * Returns the 24-bit counter_lo field value.
     *
     * @return A 24-bit unsigned integer.
     */
    public int getCounterLo() {
        return (int) subLong(9, 12);
    }

    /**
     * Returns the 32-bit entropy field value.
     *
     * @return A 32-bit unsigned integer.
     */
    public long getEntropy() {
        return subLong(12, 16);
    }

    /**
     * Digit characters used in the Base36 notation.
     */
    private static final @NotNull char[] DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    /**
     * Returns the 25-digit canonical string representation.
     */
    @Override
    public @NotNull String toString() {
        byte[] dst = new byte[25];
        int minIndex = 99; // any number greater than size of output array
        for (int i = -5; i < 16; i += 7) {
            // implement Base36 using 56-bit words
            long carry = subLong(i < 0 ? 0 : i, i + 7);

            // iterate over output array from right to left while carry != 0 but at least up to place already filled
            int j = dst.length - 1;
            for (; carry > 0 || j > minIndex; j--) {
                carry += (0xffL & dst[j]) << 56;
                dst[j] = (byte) (carry % 36);
                carry = carry / 36;
            }
            minIndex = j;
        }

        char[] chars = new char[25];
        for (int i = 0; i < 25; i++) {
            chars[i] = DIGITS[dst[i]];
        }
        return String.valueOf(chars);
    }

    /**
     * Returns {@code true} if the object is equivalent to the argument.
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
     * Returns a negative integer, zero, or positive integer if the object is less than, equal to, or greater than the
     * argument, respectively.
     */
    @Override
    public int compareTo(@NotNull Scru128Id other) {
        Objects.requireNonNull(other);
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != other.bytes[i]) {
                return Integer.compare(0xff & bytes[i], 0xff & other.bytes[i]);
            }
        }
        return 0;
    }

    /**
     * Returns a part of {@code bytes} as an unsigned long value.
     *
     * @return A long value representing {@code bytes} from {@code beginIndex} to {@code endIndex - 1}, inclusive.
     */
    private long subLong(int beginIndex, int endIndex) {
        long buffer = 0;
        while (beginIndex < endIndex) {
            buffer = (buffer << 8) | (0xffL & bytes[beginIndex++]);
        }
        return buffer;
    }
}
