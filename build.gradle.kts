import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    id("com.palantir.git-version") version "3.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "dev.thelecrafter"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")

    maven {
        url = uri("https://maven.stckoverflw.net/private")

        credentials {
            username = "blake"
            password = "GqVyx+lg/NNNIpwUKCwqzgECwzLRttCT3DUgkWUv3CxnBF9DNDDpHVDMjT5dR3lG"
        }
    }
}

dependencies {
    implementation("com.github.Minestom.Minestom:Minestom:-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("net.kyori:adventure-text-minimessage:4.13.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    implementation("net.cherrycave:birgid:0.4.0")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("net.cherrycave.lobby.LauncherKt")
    ext.set("Multi-Release", true)
}

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()
tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    processResources {
        val props = mapOf<String, Any>(
            "version" to project.version,
            "lastTag" to details.lastTag,
            "commitDistance" to details.commitDistance,
            "gitHash" to details.gitHash,
            "gitHashFull" to details.gitHashFull,
            "branchName" to details.branchName,
            "isCleanTag" to details.isCleanTag
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("constants.json") {
            expand(props)
        }
    }
}