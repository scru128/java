/**
 * SCRU128: Sortable, Clock and Random number-based Unique identifier
 * <p>
 * SCRU128 ID is yet another attempt to supersede UUID in the use cases that need decentralized, globally unique
 * time-ordered identifiers. SCRU128 is inspired by ULID and KSUID and has the following features:
 *
 * <ul>
 * <li>128-bit unsigned integer type</li>
 * <li>Sortable by generation time (as integer and as text)</li>
 * <li>26-digit case-insensitive portable textual representation</li>
 * <li>44-bit biased millisecond timestamp that ensures remaining life of 550 years</li>
 * <li>Up to 268 million time-ordered but unpredictable unique IDs per millisecond</li>
 * <li>84-bit <em>layered</em> randomness for collision resistance</li>
 * </ul>
 *
 * @see <a href="https://github.com/scru128/spec">SCRU128 Specification</a> for details.
 */
package io.github.scru128;
