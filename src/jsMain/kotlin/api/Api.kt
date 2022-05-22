package api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import org.codecranachan.roster.*

val client = HttpClient(Js) {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun addPlayer(player: Player) {
    client.post("/api/v1/players") {
        contentType(ContentType.Application.Json)
        setBody(player)
    }
}

suspend fun fetchUserId(): Identity? {
    return try {
        client.get("/api/v1/me").body()
    } catch (e: Exception) {
        null
    }
}

suspend fun fetchDiscordAccountInfo(): DiscordUserInfo? {
    return try {
        client.get("/api/v1/me/discord").body()
    } catch (e: Exception) {
        null
    }
}

suspend fun fetchLinkedGuilds(): List<Guild> {
    return try {
        client.get("/api/v1/guilds").body()
    } catch (e: Exception) {
        listOf()
    }
}

suspend fun addLinkedGuild(guild: Guild) {
    client.post("/api/v1/guilds") {
        contentType(ContentType.Application.Json)
        setBody(guild)
    }
}

suspend fun fetchEvents(guild: Guild): List<Event> {
    return client.get("/api/v1/guilds/${guild.id}/events").body()
}

suspend fun addEvent(e: Event) {
    client.post("/api/v1/events") {
        contentType(ContentType.Application.Json)
        setBody(e)
    }
}