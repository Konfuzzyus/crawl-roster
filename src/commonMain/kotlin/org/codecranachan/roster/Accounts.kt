package org.codecranachan.roster

@kotlinx.serialization.Serializable
data class DiscordUser(
    val id: String,
    val username: String,
    val avatar: String,
    val discriminator: String
)

@kotlinx.serialization.Serializable
data class DiscordGuild(
    val id: String,
    val name: String,
    val icon: String,
    val owner: Boolean,
    val permissions: String
)


@kotlinx.serialization.Serializable
data class DiscordUserInfo(
    val user: DiscordUser,
    val guilds: List<DiscordGuild>
)