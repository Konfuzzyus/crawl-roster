package org.codecranachan.roster.query

import com.benasher44.uuid.Uuid
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Table

data class ResolvedTable(
    val id: Uuid,
    val table: Table?,
    val dungeonMaster: Player?,
    val players: List<Player>
) {
    val occupancyPercent: Int = table?.let { players.size * 100 / it.details.playerRange.last } ?: 100
    val occupancyFraction: String = table?.let { "${players.size}/${it.details.playerRange.last}" } ?: "${players.size}"
    val name: String = dungeonMaster?.let { "${it.discordHandle}'s Table" } ?: "Table $id"

    fun isPlayer(playerId: Uuid): Boolean {
        return players.any { it.id == playerId }
    }

    val isFull: Boolean = table?.let { it.details.playerRange.last <= players.size } ?: false

}
