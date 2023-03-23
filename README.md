# SCRU128: Sortable, Clock and Random number-based Unique identifier

[![Maven Central](https://img.shields.io/maven-central/v/io.github.scru128/scru128)](https://central.sonatype.com/namespace/io.github.scru128)
[![License](https://img.shields.io/github/license/scru128/java)](https://github.com/scru128/java/blob/main/LICENSE)

SCRU128 ID is yet another attempt to supersede [UUID] for the users who need
decentralized, globally unique time-ordered identifiers. SCRU128 is inspired by
[ULID] and [KSUID] and has the following features:

- 128-bit unsigned integer type
- Sortable by generation time (as integer and as text)
- 25-digit case-insensitive textual representation (Base36)
- 48-bit millisecond Unix timestamp that ensures useful life until year 10889
- Up to 281 trillion time-ordered but unpredictable unique IDs per millisecond
- 80-bit three-layer randomness for global uniqueness

Kotlin examples:

```kotlin
import io.github.scru128.Scru128

// generate a new identifier object
val x = Scru128.generate()
println(x) // e.g. "036Z951MHJIKZIK2GSL81GR7L"
println(x.toByteArray()) // as a 128-bit unsigned integer in big-endian byte array

// generate a textual representation directly
println(Scru128.generateString()) // e.g. "036Z951MHZX67T63MQ9XE6Q0J"
```

Java examples:

```java
import io.github.scru128.*;

// generate a new identifier object
Scru128Id x = Scru128.generate();
System.out.println(x); // e.g. "036Z951MHJIKZIK2GSL81GR7L"
System.out.println(x.toByteArray()); // as a 128-bit unsigned integer in big-endian byte array

// generate a textual representation directly
System.out.println(Scru128.generateString()); // e.g. "036Z951MHZX67T63MQ9XE6Q0J"
```

See [SCRU128 Specification] for details.

[UUID]: https://en.wikipedia.org/wiki/Universally_unique_identifier
[ULID]: https://github.com/ulid/spec
[KSUID]: https://github.com/segmentio/ksuid
[SCRU128 Specification]: https://github.com/scru128/spec

## Installation

Obtain a copy from [Maven Central Repository]. `build.gradle.kts` example:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.scru128:scru128:<version>")
}
```

[Maven Central Repository]: https://central.sonatype.com/namespace/io.github.scru128

## Target environment

- Java 8 or higher
- Android API level 21 (Android 5.0) or higher

## License

Licensed under the Apache License, Version 2.0.

## See also

- [API Documentation](https://scru128.github.io/java/docs/)
