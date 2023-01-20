package org.codecranachan.roster.query

import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Registration
import org.codecranachan.roster.core.Table

data class ResolvedRegistration(
    val registration: Registration,
    val player: Player,
    val dungeonMaster: Player?,
    val table: Table?
) {
    val description: String? =
        when {
            dungeonMaster == null -> "Has not yet chosen a dungeon master"
            table == null -> "Waiting for ${dungeonMaster.discordMention}"
            else -> "Joining ${table.title} by ${dungeonMaster.discordMention}"
        }
}
