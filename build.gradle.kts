plugins {
    kotlin("jvm") version "1.8.20"
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
    mainClass.set("net.cherrycave.minestom.lobby.Main")
    ext.set("Multi-Release", true)
}