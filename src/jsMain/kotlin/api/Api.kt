package api

import com.benasher44.uuid.Uuid
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.LocalDate
import org.codecranachan.roster.GuildRoster
import org.codecranachan.roster.LinkedGuild
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Registration
import org.codecranachan.roster.core.Table
import org.codecranachan.roster.query.EventQueryResult
import org.codecranachan.roster.query.EventStatisticsQueryResult
import org.codecranachan.roster.query.PlayerQueryResult

val client = HttpClient(Js) {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun updatePlayer(details: Player.Details) {
    client.patch("/api/v1/me") {
        contentType(ContentType.Application.Json)
        setBody(details)
    }
}

suspend fun fetchPlayerInfo(): PlayerQueryResult? {
    return try {
        client.get("/api/v1/me").body()
    } catch (e: Exception) {
        null
    }
}

suspend fun fetchServerSettings(): GuildRoster {
    return try {
        client.get("/api/v1/guilds").body()
    } catch (e: Exception) {
        GuildRoster(0, emptyList())
    }
}

suspend fun fetchStats(
    linkedGuild: LinkedGuild,
    after: LocalDate? = null,
    before: LocalDate? = null,
): EventStatisticsQueryResult {
    return client.get("/api/v1/guilds/${linkedGuild.id}/stats") {
        after?.let { parameter("after", it) }
        before?.let { parameter("before", it) }
    }.body()
}

suspend fun fetchEvents(
    linkedGuild: LinkedGuild,
    after: LocalDate? = null,
    before: LocalDate? = null,
): List<EventQueryResult> {
    return client.get("/api/v1/guilds/${linkedGuild.id}/events") {
        after?.let { parameter("after", it) }
        before?.let { parameter("before", it) }
    }.body()
}

suspend fun addEvent(e: Event) {
    client.post("/api/v1/events") {
        contentType(ContentType.Application.Json)
        setBody(e)
    }
}

suspend fun updateEvent(eventId: Uuid, details: Event.Details) {
    client.patch("/api/v1/events/${eventId}") {
        contentType(ContentType.Application.Json)
        setBody(details)
    }
}

suspend fun deleteEvent(eventId: Uuid) {
    client.delete("/api/v1/events/${eventId}")
}

suspend fun addPlayerRegistration(eventId: Uuid, playerId: Uuid, dungeonMasterId: Uuid? = null) {
    client.put("/api/v1/events/$eventId/players/$playerId") {
        contentType(ContentType.Application.Json)
        setBody(Registration.Details(dungeonMasterId))
    }
}

suspend fun updatePlayerRegistration(eventId: Uuid, playerId: Uuid, dungeonMasterId: Uuid?) {
    client.patch("/api/v1/events/$eventId/players/$playerId") {
        contentType(ContentType.Application.Json)
        setBody(Registration.Details(dungeonMasterId))
    }
}

suspend fun removePlayerRegistration(eventId: Uuid, playerId: Uuid) {
    client.delete("/api/v1/events/$eventId/players/$playerId")
}


suspend fun addDmRegistration(eventId: Uuid, dungeonMasterId: Uuid) {
    client.put("/api/v1/events/$eventId/dms/$dungeonMasterId") {
        contentType(ContentType.Application.Json)
        setBody(Table.Details())
    }
}

suspend fun updateDmRegistration(eventId: Uuid, dungeonMasterId: Uuid, details: Table.Details) {
    client.patch("/api/v1/events/$eventId/dms/$dungeonMasterId") {
        contentType(ContentType.Application.Json)
        setBody(details)
    }
}

suspend fun removeDmRegistration(eventId: Uuid, dungeonMasterId: Uuid) {
    client.delete("/api/v1/events/$eventId/dms/$dungeonMasterId")
}
