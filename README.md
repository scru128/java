# SCRU128: Sortable, Clock and Random number-based Unique identifier

SCRU128 ID is yet another attempt to supersede [UUID] in the use cases that need
decentralized, globally unique time-ordered identifiers. SCRU128 is inspired by
[ULID] and [KSUID] and has the following features:

- 128-bit unsigned integer type
- Sortable by generation time (as integer and as text)
- 26-digit case-insensitive portable textual representation
- 44-bit biased millisecond timestamp that ensures remaining life of 550 years
- Up to 268 million time-ordered but unpredictable unique IDs per millisecond
- 84-bit _layered_ randomness for collision resistance

Kotlin examples:

```kotlin
import io.github.scru128.Scru128

// generate a new identifier object
val x = Scru128.generate()
println(x) // e.g. "00S6GVKR1MH58KE72EJD87SDOO"
println(x.toBigInteger()) // as a 128-bit unsigned integer

// generate a textual representation directly
println(Scru128.generateString()) // e.g. "00S6GVKR3F7R79I72EJF0J4RGC"
```

Java examples:

```java
import io.github.scru128.*;

// generate a new identifier object
Scru128Id x = Scru128.generate();
System.out.println(x); // e.g. "00S6GVKR1MH58KE72EJD87SDOO"
System.out.println(x.toBigInteger()); // as a 128-bit unsigned integer

// generate a textual representation directly
System.out.println(Scru128.generateString()); // e.g. "00S6GVKR3F7R79I72EJF0J4RGC"
```

See [SCRU128 Specification] for details.

[uuid]: https://en.wikipedia.org/wiki/Universally_unique_identifier
[ulid]: https://github.com/ulid/spec
[ksuid]: https://github.com/segmentio/ksuid
[scru128 specification]: https://github.com/scru128/spec

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/io.github.scru128/scru128.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.scru128%22%20AND%20a:%22scru128%22)

Obtain a copy from Maven Central Repository. `build.gradle.kts` example:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.scru128:scru128:0.6.0")
}
```

## Target environment

- Java 8 or higher
- Android API level 21 (Android 5.0) or higher

## License

Copyright 2021 LiosK

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.

## See also

- [API Documentation](https://scru128.github.io/java/docs/)
