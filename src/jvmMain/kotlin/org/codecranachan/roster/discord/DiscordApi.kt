package org.codecranachan.roster.discord

import RosterServer
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.codecranachan.roster.DiscordGuild

suspend fun fetchUserGuildInformation(accessToken: String): List<DiscordGuild> {
    return RosterServer.httpClient.get("https://discord.com/api/users/@me/guilds") {
        bearerAuth(accessToken)
    }.body()
}