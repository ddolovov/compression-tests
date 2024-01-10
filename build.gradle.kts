plugins {
    kotlin("jvm") version "2.0.0-Beta2"
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("reflect"))

    // Apache Commons Compress facade:
    implementation("org.apache.commons:commons-compress:1.25.0") { isTransitive = false }

    // XZ support:
    implementation("org.tukaani:xz:1.9") { isTransitive = false }

    // zstd support:
    implementation("com.github.luben:zstd-jni:1.5.5-11") { isTransitive = false }
}
