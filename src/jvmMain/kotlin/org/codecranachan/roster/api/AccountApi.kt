package org.codecranachan.roster.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.codecranachan.roster.DiscordUserInfo
import org.codecranachan.roster.PlayerDetails
import org.codecranachan.roster.UserSession
import org.codecranachan.roster.auth.discordOidProviderName
import org.codecranachan.roster.repo.Repository
import org.codecranachan.roster.repo.addPlayer
import org.codecranachan.roster.repo.fetchPlayerByDiscordId
import org.codecranachan.roster.repo.updatePlayer

class AccountApi(private val repository: Repository) {

    fun install(r: Route) {
        with(r) {
            get("/api/v1/me") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    // not logged in
                    call.respond(Unit)
                } else {
                    val player = repository.fetchPlayerByDiscordId(userSession.authInfo.user.id)
                        ?: repository.addPlayer(userSession.authInfo.user)
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
                    repository.updatePlayer(userSession.playerId, details)
                    call.respond(HttpStatusCode.OK)
                }
            }

            get("/api/v1/me/discord") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null || userSession.providerName != discordOidProviderName) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    call.respond(DiscordUserInfo(userSession.authInfo.user, userSession.discordGuilds))
                }
            }
        }
    }
}

