package org.codecranachan.roster.query

import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.codecranachan.roster.UuidSerializer
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Table
import kotlin.math.min

@Serializable
data class ResolvedTable(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val table: Table?,
    val dungeonMaster: Player,
    val players: List<Player>
) {
    @Transient
    val occupancyPercent: Int = table?.let { min(players.size * 100 / it.details.playerRange.last, 100) } ?: 100
    @Transient
    val occupancyFraction: String = table?.let { "${players.size}/${it.details.playerRange.last}" } ?: "${players.size}"
    @Transient
    val name: String = dungeonMaster.let { "${it.discordHandle}'s Table" }
    @Transient
    val description: String? =
        when {
            table == null -> "has not confirmed attendance"
            table.details.canceledOn != null -> "has canceled and will not be attending"
            else -> "is hosting ${table.title} ${table.settings}"
        }

    fun isPlayer(playerId: Uuid): Boolean {
        return players.any { it.id == playerId }
    }
    fun isDungeonMaster(playerId: Uuid): Boolean {
        return dungeonMaster.id == playerId
    }

    @Transient
    val isFull: Boolean = table?.let { it.details.playerRange.last <= players.size } ?: false
}
