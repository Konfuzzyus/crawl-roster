import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
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
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.title
import kotlinx.serialization.json.Json
import org.codecranachan.roster.AuthenticationSettings
import org.codecranachan.roster.Configuration
import org.codecranachan.roster.api.EventApi
import org.codecranachan.roster.api.GuildApi
import org.codecranachan.roster.api.PlayerApi
import org.codecranachan.roster.api.TestApi
import org.codecranachan.roster.auth.createDiscordOidProvider
import org.codecranachan.roster.core.RosterCore

fun HTML.index() {
    head {
        title("Crawl Roster - Adventure League planning for dummies")
        meta {
            name = "referrer"
            content = "no-referrer"
        }
    }
    body {
        div {
            id = "root"
        }
        script(src = "/crawl-roster.js") {}
    }
}

class RosterServer(private val core: RosterCore) {

    companion object {
        val httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    encodeDefaults = true
                    isLenient = true
                    allowSpecialFloatingPointValues = true
                    allowStructuredMapKeys = true
                    prettyPrint = false
                    useArrayPolymorphism = false
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    fun start() {

        val watchPaths = if (Configuration.devMode) {
            println("--- EDNA MODE ---")
            core.initForDevelopment()
            listOf("classes", "resources")
        } else {
            core.initForProduction()
            listOf()
        }

        val auth = AuthenticationSettings(
            Configuration.rootUrl, listOf(
                createDiscordOidProvider(Configuration.discordCredentials!!)
            ),
            core
        )

        embeddedServer(Netty, port = 8080, watchPaths = watchPaths) {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            install(ForwardedHeaders)
            install(XForwardedHeaders)
            auth.install(this)
            routing {
                auth.install(this)
                authenticate("auth-session", optional = false) {
                    GuildApi(core).install(this)
                    EventApi(core).install(this)
                }
                authenticate("auth-session", optional = true) {
                    PlayerApi(core).install(this)
                    get("/") {
                        call.respondHtml(HttpStatusCode.OK, HTML::index)
                    }
                    if (Configuration.devMode) {
                        TestApi(core).install(this)
                    }
                }
                if (Configuration.devMode) {
                    get("/{file...}") {
                        val file = call.parameters["file"]
                        val proxyCall = httpClient.get("http://localhost:8081/$file")
                        val contentType = proxyCall.headers["Content-Type"]?.let(ContentType::parse)
                            ?: ContentType.Application.OctetStream
                        call.respondBytes(proxyCall.readRawBytes(), contentType)
                    }
                } else {
                    staticResources("/", "")
                }
            }
        }.start(wait = true)
    }
}