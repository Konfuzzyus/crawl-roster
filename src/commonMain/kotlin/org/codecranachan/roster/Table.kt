package org.codecranachan.roster

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

enum class TableState {
    Full,
    Ready,
    Understrength,
    Empty
}

@Serializable
data class TableDetails(
    val adventureTitle: String? = null,
    val adventureDescription: String? = null,
    val moduleDesignation: String? = null,
    val language: TableLanguage = TableLanguage.English,
    @Serializable(with = IntRangeSerializer::class)
    val playerRange: IntRange = 3..7,
    @Serializable(with = IntRangeSerializer::class)
    val levelRange: IntRange = 1..4
)

@Serializable
data class Table(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    val dungeonMaster: Player,
    val details: TableDetails = TableDetails(),
    val players: List<Player> = listOf()
) {
    fun getName(): String = "${dungeonMaster.discordHandle}'s Table"

    fun getState(): TableState {
        return when {
            players.isEmpty() -> TableState.Empty
            players.size >= details.playerRange.last -> TableState.Full
            players.size <= details.playerRange.first -> TableState.Understrength
            else -> TableState.Ready
        }
    }

    fun isPlayer(player: Player): Boolean {
        return players.map { it.id }.contains(player.id)
    }

    fun isDungeonMaster(player: Player): Boolean {
        return dungeonMaster.id == player.id
    }

    fun occupancyPercentage(): Int {
        return players.size * 100 / details.playerRange.last
    }
}
