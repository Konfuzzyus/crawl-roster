package org.codecranachan.roster.query

import kotlinx.serialization.Serializable
import org.codecranachan.roster.core.Player

@Serializable
data class EventStatisticsQueryResult(
    val eventStats: EventStatistics = EventStatistics(0, 0, 0, 0),
    val dmStats: List<DungeonMasterStatistics> = emptyList()
) {
    @Serializable
    data class EventStatistics(
        val eventCount: Int,
        val tablesHosted: Int,
        val seatsFilled: Int,
        val distinctPlayers: Int
    )

    @Serializable
    data class DungeonMasterStatistics(
        val dungeonMaster: Player,
        val tablesHosted: Int,
        val seatsFilled: Int,
        val distinctPlayers: Int,
    )
}