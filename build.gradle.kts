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
}