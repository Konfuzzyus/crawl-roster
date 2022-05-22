package org.codecranachan.roster.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.codecranachan.roster.*
import org.codecranachan.roster.auth.discordOidProviderName
import org.codecranachan.roster.auth.googleOidProviderName
import org.codecranachan.roster.repo.Repository
import org.codecranachan.roster.repo.fetchPlayerByDiscordId
import org.codecranachan.roster.repo.fetchPlayerByGoogleId

class AccountApi(private val repository: Repository) {

    fun install(r: Route) {
        with(r) {
            get("/api/v1/me") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    // not logged in
                    call.respond(Unit)
                } else {
                    val profile = when (userSession.providerName) {
                        discordOidProviderName -> repository.fetchPlayerByDiscordId(userSession.user.id)
                        googleOidProviderName -> repository.fetchPlayerByGoogleId(userSession.user.id)
                        else -> null
                    }
                    // TODO: Fetch events and tables
                    call.respond(
                        Identity(
                            userSession.user.name,
                            profile,
                            emptyList(),
                            emptyList()
                        )
                    )
                }
            }

            get("/api/v1/me/discord") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null || userSession.providerName != discordOidProviderName) {
                    // not logged in
                    call.respond(Unit)
                } else {
                    val user: DiscordUser = RosterServer.httpClient.get("https://discord.com/api/users/@me") {
                        bearerAuth(userSession.accessToken)
                    }.body()
                    val guilds: List<DiscordGuild> =
                        RosterServer.httpClient.get("https://discord.com/api/users/@me/guilds") {
                            bearerAuth(userSession.accessToken)
                        }.body()
                    call.respond(DiscordUserInfo(user, guilds))
                }
            }
        }
    }
}

