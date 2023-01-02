package org.codecranachan.roster.api

import com.benasher44.uuid.Uuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.codecranachan.roster.core.RosterCore

class GuildApi(private val core: RosterCore) {

    fun install(r: Route) {
        with(r) {
            get("/api/v1/guilds/{id}/events") {
                val id = Uuid.fromString(call.parameters["id"])
                val calendar = core.eventCalendar.queryCalendar(id)
                if (calendar == null) {
                    call.respond(HttpStatusCode.NotFound)
                }else {
                    call.respond(calendar.events)
                }
            }

            get("/api/v1/guilds") {
                call.respond(core.guildRoster.get())
            }
        }
    }

}
