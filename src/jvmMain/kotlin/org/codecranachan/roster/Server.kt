package org.codecranachan.roster

import RosterServer
import index
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.html.*
import kotlinx.serialization.json.Json
import org.codecranachan.roster.api.AccountApi
import org.codecranachan.roster.api.EventApi
import org.codecranachan.roster.api.GuildApi
import org.codecranachan.roster.api.PlayerApi
import org.codecranachan.roster.auth.createDiscordOidProvider
import org.codecranachan.roster.auth.createGoogleOidProvider
import org.codecranachan.roster.repo.Repository

suspend fun main() {
    RosterServer().start()
}
