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
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.html.*
import kotlinx.serialization.json.Json
import org.codecranachan.roster.AuthenticationSettings
import org.codecranachan.roster.ClientCredentials
import org.codecranachan.roster.api.*
import org.codecranachan.roster.auth.createDiscordOidProvider
import org.codecranachan.roster.repo.FakeRepoData
import org.codecranachan.roster.repo.Repository

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
        script(src = "/static/crawl-roster.js") {}
    }
}

object Configuration {
    private val env = System.getenv()

    val devMode = env["ROSTER_DEV_MODE"] == "true"
    val rootUrl = env["ROSTER_ROOT_URL"] ?: "http://localhost:8080"
    val jdbcUri = env["ROSTER_JDBC_URI"] ?: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    val googleCredentials = getClientCreds("GOOGLE")
    val discordCredentials = getClientCreds("DISCORD")

    private val sessionEncryptKey = env["ROSTER_SESSION_ENCRYPT_KEY"] ?: "79EBA26C10A7A6D8B22864DB05987369"
    private val sessionSignKey = env["ROSTER_SESSION_SIGN_KEY"] ?: "2FFBBF335042E92C09B988DF4BC040A5"
    val sessionTransformer = SessionTransportTransformerEncrypt(hex(sessionEncryptKey), hex(sessionSignKey))

    private fun getClientCreds(prefix: String): ClientCredentials? {
        val id = env["${prefix}_CLIENT_ID"]
        val secret = env["${prefix}_CLIENT_SECRET"]
        return if (id != null && secret != null) {
            ClientCredentials(id, secret)
        } else {
            null
        }
    }
}

class RosterServer {
    companion object {
        val httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
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

    suspend fun start() {
        val repo = Repository()
        repo.migrate()

        val auth = AuthenticationSettings(
            Configuration.rootUrl,
            listOf(
                createDiscordOidProvider(Configuration.discordCredentials!!)
            )
        )

        if (Configuration.devMode) {
            println("--- EDNA MODE ---")
            FakeRepoData(repo).insert()
        }
        val watchPaths = if (Configuration.devMode) listOf("classes", "resources") else listOf()

        embeddedServer(Netty, port = 8080, watchPaths = watchPaths) {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            auth.install(this)
            routing {
                auth.install(this)
                authenticate("auth-session", optional = false) {
                    PlayerApi(repo).install(this)
                    GuildApi(repo).install(this)
                    EventApi(repo).install(this)
                    TableApi(repo).install(this)
                }
                authenticate("auth-session", optional = true) {
                    AccountApi(repo).install(this)
                    get("/") {
                        call.respondHtml(HttpStatusCode.OK, HTML::index)
                    }
                }
                static("/") {
                    resource("/favicon.ico", "favicon.ico")
                }
                if (Configuration.devMode) {
                    get("/static/{file...}") {
                        val file = call.parameters["file"]
                        val proxyCall = httpClient.get("http://localhost:8081/$file")
                        var contentType = proxyCall.headers["Content-Type"]?.let(ContentType::parse)
                            ?: ContentType.Application.OctetStream
                        call.respondBytes(proxyCall.readBytes(), contentType)
                    }
                } else {
                    static("/static") {
                        resources()
                    }
                }
            }
        }.start(wait = true)
    }
}