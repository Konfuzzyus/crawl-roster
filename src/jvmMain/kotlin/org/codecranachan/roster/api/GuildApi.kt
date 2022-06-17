package org.codecranachan.roster.api

import Configuration
import com.benasher44.uuid.Uuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.codecranachan.roster.Guild
import org.codecranachan.roster.Server
import org.codecranachan.roster.repo.Repository
import org.codecranachan.roster.repo.addLinkedGuild
import org.codecranachan.roster.repo.fetchEventsByGuild
import org.codecranachan.roster.repo.fetchLinkedGuilds

class GuildApi(private val repository: Repository) {

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
                val guild: Guild = call.receive()
                repository.addLinkedGuild(guild)
                call.respond(HttpStatusCode.OK)
            }
        }
    }

}