package org.codecranachan.roster.api

import com.benasher44.uuid.Uuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.codecranachan.roster.EventRegistration
import org.codecranachan.roster.Identity
import org.codecranachan.roster.Player
import org.codecranachan.roster.UserSession
import org.codecranachan.roster.auth.discordOidProviderName
import org.codecranachan.roster.auth.googleOidProviderName
import org.codecranachan.roster.repo.*

class PlayerApi(private val repository: Repository) {

    fun install(r: Route) {
        with(r) {

            get("/api/v1/players") {
                call.respond(repository.fetchAllPlayers())
            }

            get("/api/v1/players/{id}") {
                val id = Uuid.fromString(call.parameters["id"])
                val player = repository.fetchPlayer(id)
                if (player == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respond(player)
                }
            }

            /*
                Adds an event registration for this player.
                Available to:
                - user associated with player
                - discord bot
             */
            post("/api/v1/players/{id}/registrations") {
                val id = Uuid.fromString(call.parameters["id"])
                val registration = call.receive<EventRegistration>().copy(playerId = id)
                repository.addEventRegistration(registration)
                call.respond(HttpStatusCode.Created)
            }

            post("/api/v1/players") {
                val player = call.receive<Player>()
                val session = call.sessions.get<UserSession>()
                val existingPlayer = when (session?.providerName) {
                    googleOidProviderName -> repository.fetchPlayerByGoogleId(session.user.id)
                    discordOidProviderName -> repository.fetchPlayerByDiscordId(session.user.id)
                    else -> {
                        call.respond(HttpStatusCode.Unauthorized)
                        return@post
                    }
                }
                if (existingPlayer == null) {
                    val googleUser = if (session.providerName == googleOidProviderName) session.user else null
                    val discordUser = if (session.providerName == discordOidProviderName) session.user else null
                    repository.addPlayer(Player(player.id, player.name), discordUser, googleUser)
                    call.respond(HttpStatusCode.Created)
                } else {
                    call.respond(HttpStatusCode.Conflict)
                }
            }
        }
    }
}

