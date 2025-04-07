package org.codecranachan.roster.query

import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable
import org.codecranachan.roster.core.GuildMembership
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.TableLanguage

@Serializable
data class PlayerQueryResult(
    val player: Player,
    val memberships: List<GuildMembership> = listOf()
) {
    fun isAdminOf(guildId: Uuid) : Boolean {
        return memberships.firstOrNull { it.linkedGuild.id == guildId }?.isAdmin ?: false
    }
}

