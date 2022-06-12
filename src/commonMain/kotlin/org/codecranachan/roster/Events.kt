package org.codecranachan.roster

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

enum class TableState {
    Full,
    Ready,
    Understrength,
    Empty
}

data class TableOccupancy(val table: Table, val players: List<Player>) {
    fun getState(): TableState {
        return when {
            players.isEmpty() -> TableState.Empty
            players.size >= table.details.playerRange.last -> TableState.Full
            players.size <= table.details.playerRange.first -> TableState.Understrength
            else -> TableState.Ready
        }
    }
}

@Serializable
data class Event(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    val guildId: Uuid,
    val date: LocalDate,
    val roster: Map<Table?, List<Player>> = mapOf()
) {
    fun isRegistered(p: Player): Boolean {
        return roster.values.any { it.map(Player::id).contains(p.id) }
    }

    fun isHosting(p: Player): Boolean {
        return roster.keys.filterNotNull().map { it.dungeonMaster.id }.contains(p.id)
    }

    fun playerCount(): Int {
        return roster.values.map { it.size }.reduce(Int::plus)
    }

    fun tableSpace(): Int {
        val tables = tables()
        return if (tables.isEmpty()) 0 else tables.map { it.table.details.playerRange.last }.reduce(Int::plus)
    }

    fun tables(): List<TableOccupancy> {
        return roster.entries.filter { it.key != null }.map { TableOccupancy(it.key!!, it.value) }
    }

    fun getHostedTable(p: Player): Table? {
        return roster.keys.filterNotNull().find { it.dungeonMaster.id == p.id }
    }

    fun getFormattedDate(): String {
        return "${date.dayOfWeek.name.substring(0..2)} - ${date.dayOfMonth}. ${
            date.month.name.lowercase().replaceFirstChar { it.titlecase() }
        }, ${date.year}"
    }

}

@Serializable
data class EventRegistration(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    val eventId: Uuid,
    @Serializable(with = UuidSerializer::class)
    val playerId: Uuid,
    @Serializable(with = UuidSerializer::class)
    val tableId: Uuid? = null
)

@Serializable
data class TableHosting(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    val eventId: Uuid,
    @Serializable(with = UuidSerializer::class)
    val dungeonMasterId: Uuid
)