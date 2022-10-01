package org.codecranachan.roster.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.codecranachan.roster.Character
import org.codecranachan.roster.CharacterClass
import org.codecranachan.roster.DiscordUserInfo
import org.codecranachan.roster.PlayerDetails
import org.codecranachan.roster.UserSession
import org.codecranachan.roster.auth.discordOidProviderName
import org.codecranachan.roster.discord.fetchUserGuildInformation
import org.codecranachan.roster.dndbeyond.DndBeyondCharacter
import org.codecranachan.roster.dndbeyond.fetchCharacter
import org.codecranachan.roster.repo.Repository
import org.codecranachan.roster.repo.addCharacter
import org.codecranachan.roster.repo.addPlayer
import org.codecranachan.roster.repo.fetchPlayer
import org.codecranachan.roster.repo.getGuildMemberships
import org.codecranachan.roster.repo.getPlayerCharacters
import org.codecranachan.roster.repo.updatePlayer

fun DndBeyondCharacter.extractClasses(): List<CharacterClass> {
    return classes.map { c ->
        CharacterClass(
            level = c.level,
            name = c.definition.name
        )
    }
}

class AccountApi(private val repository: Repository) {

    fun install(r: Route) {
        with(r) {
            get("/api/v1/me") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    // not logged in
                    call.respond(Unit)
                } else {
                    val player = repository.fetchPlayer(userSession.playerId)
                        ?: repository.addPlayer(userSession.authInfo.user)
                    val memberships = repository.getGuildMemberships(userSession.playerId)
                    val characters = repository.getPlayerCharacters(userSession.playerId)
                    call.respond(player.copy(memberships = memberships, characters = characters))
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
                    val guilds = fetchUserGuildInformation(userSession.accessToken)
                    call.respond(DiscordUserInfo(userSession.authInfo.user, guilds))
                }
            }

            post("api/v1/me/characters") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null || userSession.providerName != discordOidProviderName) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    val character = call.receive<Character>()
                    val response = character.dndBeyondId?.let { id -> fetchCharacter(id) }

                    if (response == null || !response.success) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid D&D Beyond character id")
                    } else {
                        val dndCharacter = response.data
                        val imported = character.copy(
                            name = dndCharacter.name ?: "Unnamed Hero",
                            classes = dndCharacter.extractClasses()
                        )
                        repository.addCharacter(userSession.playerId, imported)
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }
    }
}

