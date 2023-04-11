plugins {
    kotlin("jvm") version "1.8.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "dev.thelecrafter"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.Minestom.Minestom:Minestom:8ad2c7701f")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("net.cherrycave.minestom.lobby.MainKt")
}