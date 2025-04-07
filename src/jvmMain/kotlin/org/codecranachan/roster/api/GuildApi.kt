package org.codecranachan.roster.api

import com.benasher44.uuid.Uuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import org.codecranachan.roster.core.RosterCore

class GuildApi(private val core: RosterCore) {

    fun install(r: Route) {
        with(r) {
            get("/api/v1/guilds/{id}/events") {
                try {
                    val id = Uuid.fromString(call.parameters["id"])
                    val after = call.request.queryParameters["after"]?.let { LocalDate.parse(it) }
                    val before = call.request.queryParameters["before"]?.let { LocalDate.parse(it) }
                    val calendar = core.eventCalendar.queryCalendar(id, after, before)
                    if (calendar == null) {
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        call.respond(calendar.events)
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/api/v1/guilds") {
                call.respond(core.guildRoster.get())
            }

            get("/api/v1/guilds/{id}/stats") {
                try {
                    val id = Uuid.fromString(call.parameters["id"])
                    val after = call.request.queryParameters["after"]?.let { LocalDate.parse(it) }
                    val before = call.request.queryParameters["before"]?.let { LocalDate.parse(it) }
                    val stats = core.eventCalendar.queryStatistics(id, after, before)
                    if (stats == null) {
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        call.respond(stats)
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

        }
    }

}
