package org.codecranachan.roster.query

import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Registration
import org.codecranachan.roster.core.Table

@Serializable
data class EventQueryResult(
    val event: Event,
     val rawRegistrations: List<Registration>,
     val rawTables: List<Table>,
     val rawPlayers: List<Player>
) {
    @Transient
    val players: Map<Uuid, Player> = rawPlayers.associateBy { it.id }

    @Transient
    val tables: Map<Uuid, ResolvedTable> = resolveTables()

    @Transient
    val registrations: List<ResolvedRegistration> = resolveRegistrations()

    @Transient
    val unseated = registrations.filter { it.dm == null }.map { it.player }

    fun isRegistered(playerId: Uuid): Boolean {
        return rawRegistrations.any { it.playerId == playerId }
    }

    fun isHosting(playerId: Uuid): Boolean {
        return rawTables.any { it.dungeonMasterId == playerId }
    }

    val playerCount: Int = rawRegistrations.size

    val tableSpace: Int = rawTables.sumOf { it.details.playerRange.last }

    private fun resolveTables(): Map<Uuid, ResolvedTable> {
        val regs = rawRegistrations.groupBy { it.details.dungeonMasterId }
        val tabs = rawTables.associateBy { it.dungeonMasterId }
        val dmIds = regs.keys.union(tabs.keys)

        return dmIds.filterNotNull().map { dmId ->
            ResolvedTable(
                dmId,
                tabs[dmId],
                players[dmId],
                (regs[dmId] ?: emptyList()).mapNotNull { players[it.playerId] }
            )
        }.associateBy { it.id }
    }

    private fun resolveRegistrations(): List<ResolvedRegistration> {
        return rawRegistrations
            .sortedByDescending { it.meta.registrationDate }
            .mapNotNull {
                players[it.playerId]?.let { p ->
                    ResolvedRegistration(it, p, players[it.details.dungeonMasterId], tables[it.details.dungeonMasterId]?.table)
                }
            }

    }
}