package org.codecranachan.roster.query

import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable
import org.codecranachan.roster.UuidSerializer
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.TableLanguage

@Serializable
data class EventStatisticsQueryResult(
    @Serializable(with = UuidSerializer::class)
    val linkedGuildId: Uuid,
    val eventStats: EventStatistics,
    val dmStats: List<DungeonMasterStatistics>
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
        val hostedLanguages: Set<TableLanguage>,
        val seatsFilled: Int,
        val distinctPlayers: Int,
    )
}