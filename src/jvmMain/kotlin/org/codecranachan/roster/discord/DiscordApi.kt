package org.codecranachan.roster.discord

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.codecranachan.roster.DiscordGuild

class DiscordApiClient(private val client: HttpClient,
                       private val accessToken: String) {
    suspend fun fetchUserGuildInformation(): List<DiscordGuild> {
        return client.get("https://discord.com/api/users/@me/guilds") {
            bearerAuth(accessToken)
        }.body()
    }
}

