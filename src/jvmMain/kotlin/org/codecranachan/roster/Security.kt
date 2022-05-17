package org.codecranachan.roster

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.p
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwk.HttpsJwks
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver

data class UserSession(val accessToken: String, val idToken: String) : Principal

@kotlinx.serialization.Serializable
data class OpenIdConfiguration(
    val issuer: String,
    val authorization_endpoint: String,
    val device_authorization_endpoint: String,
    val token_endpoint: String,
    val userinfo_endpoint: String,
    val revocation_endpoint: String,
    val jwks_uri: String,
    val response_types_supported: List<String>,
    val subject_types_supported: List<String>,
    val id_token_signing_alg_values_supported: List<String>,
    val scopes_supported: List<String>,
    val token_endpoint_auth_methods_supported: List<String>,
    val claims_supported: List<String>,
    val code_challenge_methods_supported: List<String>,
    val grant_types_supported: List<String>
)

object ClientCredentials {
    const val id = "1019714989830-94g4bdinitqv5gd5ugvndqqbjc2l4v7j.apps.googleusercontent.com"
    const val secret = "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"
}

object SessionSecrets {
    private val secretEncryptKey = hex("00112233445566778899aabbccddeeff")
    private val secretSignKey = hex("6819b57a326945c1968f45236589")
    val transformer = SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey)
}

class AuthenticationSettings(private val oidConf: OpenIdConfiguration) {

    private val _httpsJkws = HttpsJwks(oidConf.jwks_uri)
    private val _httpsJwksKeyResolver = HttpsJwksVerificationKeyResolver(_httpsJkws)

    val jwtConsumer = JwtConsumerBuilder()
        .setVerificationKeyResolver(_httpsJwksKeyResolver)
        .setJwsAlgorithmConstraints(
            AlgorithmConstraints.ConstraintType.PERMIT,
            AlgorithmIdentifiers.RSA_USING_SHA256
        )
        .setExpectedIssuer(oidConf.issuer)
        .setExpectedAudience(ClientCredentials.id)
        .build()

    val settings = OAuthServerSettings.OAuth2ServerSettings(
        name = "google",
        authorizeUrl = oidConf.authorization_endpoint,
        accessTokenUrl = oidConf.token_endpoint,
        requestMethod = HttpMethod.Post,
        clientId = ClientCredentials.id,
        clientSecret = ClientCredentials.secret,
        defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile")
    )

    fun install(app: Application) {
        with(app) {
            install(Sessions) {
                cookie<UserSession>("user_session") {
                    cookie.path = "/"
                    cookie.maxAgeInSeconds = 60
                    transform(SessionSecrets.transformer)
                }
            }
            install(Authentication) {
                oauth("auth-oauth-google") {
                    urlProvider = { "http://localhost:8080/auth/callback" }
                    providerLookup = { settings }
                    client = RosterServer.httpClient
                }
                session<UserSession>("auth-session") {
                    validate { session ->
                        try {
                            jwtConsumer.processToClaims(session.idToken)
                            session
                        } catch (e: InvalidJwtException) {
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
            authenticate("auth-oauth-google") {
                get("/auth/login/auth-oauth-google") { }
                get("/auth/callback") {
                    val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                    call.sessions.set(
                        UserSession(
                            principal?.accessToken.toString(),
                            principal?.extraParameters?.get("id_token").toString()
                        )
                    )
                    call.respondRedirect("/")
                }
            }
            authenticate("auth-session", optional = false) {
                get("/auth/user") {
                    val userSession = call.sessions.get<UserSession>()
                    if (userSession == null) {
                        call.respond(Unit)
                    } else {
                        val claims = jwtConsumer.processToClaims(userSession.idToken)
                        val user = UserIdentity(
                            claims.claimsMap["name"] as String,
                            claims.claimsMap["picture"] as String?,
                            claims.claimsMap["given_name"] as String?,
                            claims.claimsMap["locale"] as String?
                        )
                        call.respond(user)
                    }
                }
            }
            authenticate("auth-session", optional = true) {
                get("/auth/login") {
                    call.respondHtml {
                        body {
                            p {
                                a("/auth/login/auth-oauth-google") { +"Login with Google" }
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