package org.codecranachan.roster.auth

import io.ktor.client.call.*
import io.ktor.client.request.*
import org.codecranachan.roster.ClientCredentials
import org.codecranachan.roster.OpenIdProvider
import org.codecranachan.roster.RosterServer
import org.codecranachan.roster.UserIdentity

const val googleOidProviderName = "google"

suspend fun createGoogleOidProvider() = OpenIdProvider(
    googleOidProviderName,
    ClientCredentials(
        id = "1019714989830-94g4bdinitqv5gd5ugvndqqbjc2l4v7j.apps.googleusercontent.com",
        secret = "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"
    ),
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