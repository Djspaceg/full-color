plugins {
    kotlin("jvm") version "2.2.0"
    `maven-publish`
    `java-library`
}

group = "com.resourcefork"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
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
            groupId = "com.resourcefork"
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

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Djspaceg/full-color")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String?
            }
        }
    }
}
