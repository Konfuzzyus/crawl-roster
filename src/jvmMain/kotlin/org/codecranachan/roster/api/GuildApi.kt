package org.codecranachan.roster.api

import Configuration
import com.benasher44.uuid.Uuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.codecranachan.roster.Guild
import org.codecranachan.roster.Server
import org.codecranachan.roster.UserSession
import org.codecranachan.roster.repo.Repository
import org.codecranachan.roster.repo.addLinkedGuild
import org.codecranachan.roster.repo.fetchEventsByGuild
import org.codecranachan.roster.repo.fetchLinkedGuilds

class GuildApi(private val repository: Repository) {
    private val permissions = Permissions(repository)

    fun install(r: Route) {
        with(r) {
            get("/api/v1/guilds/{id}/events") {
                val id = Uuid.fromString(call.parameters["id"])
                call.respond(repository.fetchEventsByGuild(id))
            }

            get("/api/v1/guilds") {
                call.respond(
                    Server(
                        Configuration.guildLimit,
                        repository.fetchLinkedGuilds()
                    )
                )
            }

            post("/api/v1/guilds") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    val guild: Guild = call.receive()
                    if (repository.fetchLinkedGuilds().size >= Configuration.guildLimit) {
                        call.respond(HttpStatusCode.Conflict, "May not attune any more guilds to this server")
                    } else if (permissions.hasAdminRightsForGuild(userSession.playerId, guild)) {
                        repository.addLinkedGuild(guild)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Must be a guild admin to attune a guild")
                    }
                }
            }
        }
    }

}