plugins {
    kotlin("jvm") version "2.1.21"
    `maven-publish`
    `java-library`
}

group = "com.djspaceg"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "com.djspaceg"
            artifactId = "full-color"
            version = "1.0.0"

            pom {
                name.set("full-color")
                description.set("Kotlin-based color utility library supporting wide-gamut OKLab color space, multiple color format inputs/outputs, color ramps, gradients, and manipulation utilities.")
                url.set("https://github.com/Djspaceg/full-color")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("djspaceg")
                        name.set("Djspaceg")
                        url.set("https://github.com/Djspaceg")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Djspaceg/full-color.git")
                    developerConnection.set("scm:git:ssh://github.com/Djspaceg/full-color.git")
                    url.set("https://github.com/Djspaceg/full-color")
                }
            }
        }
    }
}
