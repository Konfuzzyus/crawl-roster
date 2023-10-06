package org.codecranachan.roster.query

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Registration
import org.codecranachan.roster.core.Table

@Serializable
data class ResolvedRegistration(
    val registration: Registration,
    val player: Player,
    val dungeonMaster: Player?,
    val table: Table?
) {
    @Transient
    val description: String? =
        when {
            dungeonMaster == null -> "Has not yet chosen a dungeon master"
            table == null -> "Waiting for ${dungeonMaster.discordMention}"
            else -> "Joining ${table.title} by ${dungeonMaster.discordMention}"
        }
}
