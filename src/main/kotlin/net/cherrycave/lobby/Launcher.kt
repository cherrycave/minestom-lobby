package net.cherrycave.lobby

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.cherrycave.birgid.GertrudClient
import net.cherrycave.birgid.command.ServerType
import net.cherrycave.birgid.command.registerServer
import net.cherrycave.lobby.data.ConfigData
import net.minestom.server.coordinate.Pos
import kotlin.io.path.*
import kotlin.system.exitProcess

val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

private val dataPath = Path("./data")

private val configFile = dataPath.resolve("config.json")

val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}

val gertrudClient by lazy {
    GertrudClient {
        host = System.getenv("BACKEND_HOST") ?: "localhost"
        port = System.getenv("BACKEND_PORT")?.toInt() ?: 6969
        apiKey = System.getenv("BACKEND_API_KEY") ?: error("No API Key provided")
        identifier = System.getenv("P_SERVER_UUID") ?: "lobby"
        https = System.getenv("HTTPS")?.toBoolean() ?: true
    }
}

suspend fun main(args: Array<String>) {
    val production = System.getenv("P_SERVER_UUID") != null

    val (host, port) = if (production) {


        val registration = gertrudClient.registerServer(true, ServerType.LOBBY)

        registration.onFailure {
            println("Failed to register server: ${it.message}")
            exitProcess(1)
        }

        val registrationData = registration.getOrNull() ?: error("Failed to register server")

        Runtime.getRuntime().addShutdownHook(Thread {
            coroutineScope.launch {
                gertrudClient.registerServer(false, ServerType.LOBBY)
            }
        })

        registrationData.host to registrationData.port
    } else {
        dataPath.createDirectories()
        if (!configFile.exists() || args.firstOrNull() == "--generateConfig") {
            println("Generating config...")
            configFile.writeText(json.encodeToString(ConfigData(Pos.ZERO, emptyList())))
            exitProcess(0)
        }

        "0.0.0.0" to 25565
    }

    CherryLobby(
        host,
        port,
        production,
    )
}