package org.codecranachan.roster.auth

import RosterServer
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.codecranachan.roster.ClientCredentials
import org.codecranachan.roster.DiscordUser
import org.codecranachan.roster.OpenIdConfiguration
import org.codecranachan.roster.OpenIdProvider
import org.codecranachan.roster.UserIdentity

@kotlinx.serialization.Serializable
data class DiscordAuthorizationInfo(
    val scopes: List<String>,
    val expires: String,
    val user: DiscordUser
)

const val discordOidProviderName = "discord"

fun createDiscordOidProvider(credentials: ClientCredentials) = OpenIdProvider(
    discordOidProviderName,
    credentials,
    OpenIdConfiguration(
        authorization_endpoint = "https://discord.com/api/oauth2/authorize",
        token_endpoint = "https://discord.com/api/oauth2/token",
        userinfo_endpoint = "https://discord.com/api/oauth2/@me",
        revocation_endpoint = "https://discord.com/api/oauth2/token/revoke",
    ),
    listOf("identify", "guilds"),
) { principal, provider ->
    val info: DiscordAuthorizationInfo = RosterServer.httpClient.get(provider.conf.userinfo_endpoint) {
        bearerAuth(principal.accessToken)
    }.body()
    UserIdentity(
        info.user.id,
        info.user.username,
        "https://cdn.discordapp.com/avatars/${info.user.id}/${info.user.avatar}"
    )
}
