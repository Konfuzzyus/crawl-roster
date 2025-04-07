package org.codecranachan.roster.query

import kotlinx.serialization.Serializable
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Table

@Serializable
data class TableQueryResult(
    val event: Event,
    val table: Table,
    val dm: Player,
    val players: List<Player> = listOf(),
) {
    fun getTableName(): String = dm.getTableName()

    fun getState(): TableState {
        return when {
            players.isEmpty() -> TableState.Empty
            players.size >= table.details.playerRange.last -> TableState.Full
            players.size <= table.details.playerRange.first -> TableState.Understrength
            else -> TableState.Ready
        }
    }

    fun isPlayer(player: Player): Boolean {
        return players.map { it.id }.contains(player.id)
    }

    fun isDungeonMaster(player: Player): Boolean {
        return dm.id == player.id
    }

    fun isFull() = players.size >= table.details.playerRange.endInclusive
}
