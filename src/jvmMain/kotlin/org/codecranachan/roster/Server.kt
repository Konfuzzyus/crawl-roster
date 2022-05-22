package org.codecranachan.roster

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.serialization.json.Json
import org.codecranachan.roster.api.AccountApi
import org.codecranachan.roster.api.EventApi
import org.codecranachan.roster.api.GuildApi
import org.codecranachan.roster.api.PlayerApi
import org.codecranachan.roster.auth.createDiscordOidProvider
import org.codecranachan.roster.auth.createGoogleOidProvider
import org.codecranachan.roster.repo.Repository

fun HTML.index() {
    head {
        title("Crawl Roster - Adventure League planning for dummies")
    }
    body {
        div {
            id = "root"
        }
        script(src = "/static/crawl-roster.js") {}
    }
}

class RosterServer {
    companion object {
        val httpClient = HttpClient(CIO) {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(
                    Json {
                        encodeDefaults = true
                        isLenient = true
                        allowSpecialFloatingPointValues = true
                        allowStructuredMapKeys = true
                        prettyPrint = false
                        useArrayPolymorphism = false
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }
}

suspend fun main() {
    val repo = Repository()
    repo.migrate()

    val auth = AuthenticationSettings(
        "http://localhost:8080",
        listOf(
            createDiscordOidProvider(),
            createGoogleOidProvider()
        )
    )

    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        install(ContentNegotiation) {
            json()
        }
        auth.install(this)
        routing {
            auth.install(this)
            authenticate("auth-session", optional = false) {
                PlayerApi(repo).install(this)
                GuildApi(repo).install(this)
                EventApi(repo).install(this)
            }
            authenticate("auth-session", optional = true) {
                AccountApi(repo).install(this)
                get("/") {
                    call.respondHtml(HttpStatusCode.OK, HTML::index)
                }
            }
            static("/") {
                resource("/favicon.ico","favicon.ico")
            }
            static("/static") {
                resources()
            }
        }
    }.start(wait = true)
}
