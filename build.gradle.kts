plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.resourcefork"
version = "1.2.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
}

kotlin {
    jvmToolchain(21)
}
