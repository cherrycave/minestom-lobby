package net.cherrycave.minestom.lobby.data

import kotlinx.serialization.Serializable

@Serializable
data class Constants(
    val version: String,
    val lastTag: String,
    val commitDistance: String,
    val gitHash: String,
    val gitHashFull: String,
    val branchName: String,
    val isCleanTag: String
)