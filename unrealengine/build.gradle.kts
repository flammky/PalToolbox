plugins {
    kotlin("jvm")
}

group = "dev.dexsr.gmod.palworld.trainer"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    // will we really use okio ?
    implementation("com.squareup.okio:okio:3.8.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}