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

println(Scru128.scru128()) // e.g. "00R5B73KHDE4DDE3K37FIIVA8H"
println(Scru128.scru128()) // e.g. "00R5B73KHDE4DDG3K37CD8D04L"
```

Java examples:

```java
import io.github.scru128.Scru128;

System.out.println(Scru128.scru128()); // e.g. "00R5B73KHDE4DDE3K37FIIVA8H"
System.out.println(Scru128.scru128()); // e.g. "00R5B73KHDE4DDG3K37CD8D04L"
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
    implementation("io.github.scru128:scru128:0.3.0")
}
```

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
