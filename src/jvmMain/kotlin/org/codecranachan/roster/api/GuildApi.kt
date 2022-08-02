package org.codecranachan.roster.api

import com.benasher44.uuid.Uuid
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.codecranachan.roster.logic.RosterCore

class GuildApi(private val core: RosterCore) {

    fun install(r: Route) {
        with(r) {
            get("/api/v1/guilds/{id}/events") {
                val id = Uuid.fromString(call.parameters["id"])
                call.respond(core.eventCalendar.get(id).events)
            }

            get("/api/v1/guilds") {
                call.respond(core.guildRoster.get())
            }
        }
    }

}
