package org.codecranachan.roster

import Configuration
import RosterServer
import com.benasher44.uuid.Uuid
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.p
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.codecranachan.roster.auth.DiscordAuthorizationInfo
import org.codecranachan.roster.repo.Repository
import org.codecranachan.roster.repo.addPlayer
import org.codecranachan.roster.repo.fetchPlayerByDiscordId
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Serializable
data class UserSession(
    val providerName: String,
    val accessToken: String,
    val expiresIn: Long,
    @Serializable(with = UuidSerializer::class)
    val playerId: Uuid,
    val authInfo: DiscordAuthorizationInfo,
    val discordGuilds: List<DiscordGuild>
) : Principal

@Serializable
data class OpenIdConfiguration(
    val issuer: String = "",
    val authorization_endpoint: String,
    val device_authorization_endpoint: String = "",
    val token_endpoint: String,
    val userinfo_endpoint: String,
    val revocation_endpoint: String,
    val jwks_uri: String = "",
    val response_types_supported: List<String> = emptyList(),
    val subject_types_supported: List<String> = emptyList(),
    val id_token_signing_alg_values_supported: List<String> = emptyList(),
    val scopes_supported: List<String> = emptyList(),
    val token_endpoint_auth_methods_supported: List<String> = emptyList(),
    val claims_supported: List<String> = emptyList(),
    val code_challenge_methods_supported: List<String> = emptyList(),
    val grant_types_supported: List<String> = emptyList()
)

data class ClientCredentials(
    val id: String,
    val secret: String
)

fun interface IdentityRetrievalStrategy {
    suspend operator fun invoke(
        auth: OAuthAccessTokenResponse.OAuth2,
        provider: OpenIdProvider
    ): DiscordAuthorizationInfo
}

data class OpenIdProvider(
    val name: String,
    val cred: ClientCredentials,
    val conf: OpenIdConfiguration,
    val scopes: List<String>,
    val identitySupplier: IdentityRetrievalStrategy
) {
    val settings = OAuthServerSettings.OAuth2ServerSettings(
        name = conf.issuer,
        authorizeUrl = conf.authorization_endpoint,
        accessTokenUrl = conf.token_endpoint,
        requestMethod = HttpMethod.Post,
        clientId = cred.id,
        clientSecret = cred.secret,
        defaultScopes = scopes
    )
}

class JsonSessionSerializer : SessionSerializer<UserSession> {
    override fun deserialize(text: String): UserSession {
        return Json.decodeFromString(UserSession.serializer(), text)
    }

    override fun serialize(session: UserSession): String {
        return Json.encodeToString(UserSession.serializer(), session)
    }
}

class AuthenticationSettings(
    private val rootUrl: String,
    private val providers: List<OpenIdProvider>,
    private val repository: Repository
) {
    private val sessionExiprationTime = 7.toDuration(DurationUnit.DAYS)

    fun install(app: Application) {
        with(app) {
            install(Sessions) {
                cookie<UserSession>("user_session") {
                    cookie.path = "/"
                    cookie.maxAgeInSeconds = sessionExiprationTime.inWholeSeconds
                    cookie.extensions["SameSite"] = "lax"
                    serializer = JsonSessionSerializer()
                    transform(Configuration.sessionTransformer)
                }
            }
            install(Authentication) {
                providers.forEach { oidProvider ->
                    oauth(oidProvider.name) {
                        urlProvider = { "${rootUrl}/auth/${oidProvider.name}/callback" }
                        providerLookup = { oidProvider.settings }
                        client = RosterServer.httpClient
                    }
                }
                session<UserSession>("auth-session") {
                    validate { session ->
                        if (Instant.fromEpochSeconds(session.expiresIn) < Clock.System.now()) {
                            session
                        } else {
                            null
                        }
                    }
                    challenge {
                        call.respondRedirect("/auth/login")
                    }
                }
            }
        }
    }

    fun install(r: Routing) {
        with(r) {
            providers.forEach { oidProvider ->
                authenticate(oidProvider.name) {
                    get("/auth/${oidProvider.name}/login") { }
                    get("/auth/${oidProvider.name}/callback") {
                        val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                        val authInfo = oidProvider.identitySupplier(principal!!, oidProvider)

                        var player = repository.fetchPlayerByDiscordId(authInfo.user.id)
                        if (player == null) {
                            player = repository.addPlayer(authInfo.user)
                        }

                        val guilds: List<DiscordGuild> =
                            RosterServer.httpClient.get("https://discord.com/api/users/@me/guilds") {
                                bearerAuth(principal.accessToken)
                            }.body()

                        call.sessions.set(
                            UserSession(
                                oidProvider.name,
                                principal.accessToken,
                                principal.expiresIn,
                                player.id,
                                authInfo,
                                guilds
                            )
                        )
                        call.respondRedirect("/")
                    }
                }
            }
            authenticate("auth-session", optional = true) {
                get("/auth/login") {
                    call.respondHtml {
                        body {
                            providers.forEach { oidProvider ->
                                p {
                                    a("/auth/${oidProvider.name}/login") { +"Login with ${oidProvider.name}" }
                                }
                            }
                        }
                    }
                }
                get("/auth/logout") {
                    call.sessions.clear<UserSession>()
                    call.respondRedirect("/")
                }
            }
        }
    }
}