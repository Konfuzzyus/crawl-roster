package org.codecranachan.roster

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
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
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.jose4j.jwt.consumer.InvalidJwtException

fun HTML.index() {
    head {
        title("Hello from Ktor!")
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
                json()
            }
        }
    }
}

suspend fun main() {
    val repo = Repository()
    repo.migrate()

    val gOidConf: OpenIdConfiguration =
        RosterServer.httpClient.get("https://accounts.google.com/.well-known/openid-configuration").body()
    val auth = AuthenticationSettings(gOidConf)

    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        install(ContentNegotiation) {
            json()
        }
        auth.install(this)
        routing {
            auth.install(this)
            authenticate("auth-session", optional = false) {
                PlayerApi(repo).install(this)
            }
            authenticate("auth-session", optional = true) {
                get("/") {
                    val userSession = call.sessions.get<UserSession>()
                    if (userSession == null) {
                        call.respondRedirect("/auth/login")
                    } else {
                        call.respondHtml(HttpStatusCode.OK, HTML::index)
                    }
                }
            }
            static("/static") {
                resources()
            }
        }
    }.start(wait = true)
}
