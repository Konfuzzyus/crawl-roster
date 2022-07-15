package org.codecranachan.roster.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.codecranachan.roster.PlayerDetails
import org.codecranachan.roster.RosterCore
import org.codecranachan.roster.UserSession

class PlayerApi(private val core: RosterCore) {

    fun install(r: Route) {
        with(r) {
            get("/api/v1/me") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    // not logged in
                    call.respond(Unit)
                } else {
                    val player = core.playerRoster.registerDiscordPlayer(userSession.authInfo.user)
                    call.respond(player)
                }
            }

            patch("/api/v1/me") {
                val userSession = call.sessions.get<UserSession>()
                val details = call.receive<PlayerDetails>()
                if (userSession == null) {
                    // not logged in
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    core.playerRoster.updatePlayer(userSession.playerId, details)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}

