package org.codecranachan.roster

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class PlayerDetails(
    val name: String = "Anonymous",
    val languages: List<TableLanguage> = listOf(TableLanguage.English),
    val playTier: Int = 0
)

@Serializable
data class Player(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    val discordHandle: String,
    val avatarUrl: String? = null,
    val details: PlayerDetails = PlayerDetails(),
    val memberships: List<GuildMembership> = emptyList(),
    val isServerAdmin: Boolean = false
) {
    fun isAdminOf(guild: Guild): Boolean {
        return memberships.firstOrNull { it.guild.id == guild.id }?.isAdmin ?: false
    }
}

@Serializable
data class GuildMembership(
    val guild: Guild,
    val isAdmin: Boolean,
    val isDungeonMaster: Boolean
)
