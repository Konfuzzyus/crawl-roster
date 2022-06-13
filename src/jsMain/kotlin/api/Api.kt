package api

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import org.codecranachan.roster.DiscordUserInfo
import org.codecranachan.roster.Event
import org.codecranachan.roster.EventRegistration
import org.codecranachan.roster.Guild
import org.codecranachan.roster.Identity
import org.codecranachan.roster.Player
import org.codecranachan.roster.PlayerDetails
import org.codecranachan.roster.Table
import org.codecranachan.roster.TableDetails
import org.codecranachan.roster.TableHosting

val client = HttpClient(Js) {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun updatePlayer(details: PlayerDetails) {
    client.patch("/api/v1/me") {
        contentType(ContentType.Application.Json)
        setBody(details)
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

suspend fun addEventRegistration(e: Event, p: Player) {
    client.post("/api/v1/events/${e.id}/registrations") {
        contentType(ContentType.Application.Json)
        setBody(EventRegistration(uuid4(), e.id, p.id))
    }
}

suspend fun updateEventRegistration(e: Event, p: Player, t: Table?) {
    client.patch("/api/v1/events/${e.id}/registrations/${p.id}") {
        contentType(ContentType.Application.Json)
        setBody(EventRegistration(uuid4(), e.id, p.id, t?.id))
    }
}

suspend fun removeEventRegistration(e: Event, p: Player) {
    client.delete("/api/v1/events/${e.id}/registrations/${p.id}")
}


suspend fun addTableHosting(e: Event, dm: Player) {
    client.post("/api/v1/events/${e.id}/tables") {
        contentType(ContentType.Application.Json)
        setBody(TableHosting(uuid4(), e.id, dm.id))
    }
}

suspend fun removeTableHosting(e: Event, dm: Player) {
    client.delete("/api/v1/events/${e.id}/tables/${dm.id}")
}

suspend fun updateTableHosting(id: Uuid, details: TableDetails) {
    client.patch("/api/v1/tables/${id}") {
        contentType(ContentType.Application.Json)
        setBody(details)
    }
}