package org.codecranachan.roster

@kotlinx.serialization.Serializable
data class DiscordUser(
    val id: String,
    val username: String,
    val avatar: String? = null,
    val discriminator: String = "0"
) {
    fun getAvatarUrl(): String? = avatar?.let { "https://cdn.discordapp.com/avatars/${id}/${it}" }
}

@kotlinx.serialization.Serializable
data class DiscordGuild(
    val id: String,
    val name: String,
    val owner: Boolean,
    val permissions: String
) {
    companion object {
        private const val ADMINISTRATOR_FLAG: Long = 0x8L
    }

    fun isAdmin(): Boolean = permissions.toLong().and(ADMINISTRATOR_FLAG) != 0L
}


@kotlinx.serialization.Serializable
data class DiscordUserInfo(
    val user: DiscordUser,
    val guilds: List<DiscordGuild>
) {
    fun hasAdminRightsFor(guild: Guild): Boolean {
        return guilds.filter { it.id == guild.discordId }.any { it.isAdmin() || it.owner }
    }
}