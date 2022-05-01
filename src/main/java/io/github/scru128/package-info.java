/**
 * SCRU128: Sortable, Clock and Random number-based Unique identifier
 * <p>
 * SCRU128 ID is yet another attempt to supersede UUID for the users who need decentralized, globally unique
 * time-ordered identifiers. SCRU128 is inspired by ULID and KSUID and has the following features:
 *
 * <ul>
 * <li>128-bit unsigned integer type</li>
 * <li>Sortable by generation time (as integer and as text)</li>
 * <li>25-digit case-insensitive textual representation (Base36)</li>
 * <li>48-bit millisecond Unix timestamp that ensures useful life until year 10889</li>
 * <li>Up to 281 trillion time-ordered but unpredictable unique IDs per millisecond</li>
 * <li>80-bit three-layer randomness for global uniqueness</li>
 * </ul>
 *
 * @see <a href="https://github.com/scru128/spec">SCRU128 Specification</a>
 */
package io.github.scru128;
