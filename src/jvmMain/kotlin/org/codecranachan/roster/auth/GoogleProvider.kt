package org.codecranachan.roster.auth

import io.ktor.client.call.*
import io.ktor.client.request.*
import org.codecranachan.roster.ClientCredentials
import org.codecranachan.roster.OpenIdProvider
import org.codecranachan.roster.UserIdentity

const val googleOidProviderName = "google"

suspend fun createGoogleOidProvider(credentials: ClientCredentials) = OpenIdProvider(
    googleOidProviderName,
    credentials,
    RosterServer.httpClient.get("https://accounts.google.com/.well-known/openid-configuration").body(),
    listOf("https://www.googleapis.com/auth/userinfo.profile")
) { principal, provider ->
    val token = principal.extraParameters["id_token"]!!
    val claims = provider.jwtConsumer.processToClaims(token)
    UserIdentity(
        claims.subject,
        claims.claimsMap["name"] as String,
        claims.claimsMap["picture"] as String?
    )
}