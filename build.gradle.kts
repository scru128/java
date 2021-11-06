plugins {
    `java-library`
    `maven-publish`
}

group = "net.liosk.scru128"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // Use @NotNull and @Nullable annotations for Kotlin interoperability
    compileOnly("org.jetbrains:annotations:22.0.0")

    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

tasks.test {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

java {
    // Publish sources jar and javadoc jar
    withSourcesJar()
    withJavadocJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("${project.group}:${project.name}")
                description.set("SCRU128: Sortable, Clock and Random number-based Unique identifier")
                url.set("https://github.com/scru128/java")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }
                developers {
                    developer {
                        name.set("LiosK")
                        email.set("contact@mail.liosk.net")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/scru128/java.git")
                    developerConnection.set("scm:git:ssh://git@github.com/scru128/java.git")
                    url.set("https://github.com/scru128/java")
                }
            }
        }
    }
}
