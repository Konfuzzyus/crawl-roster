package org.codecranachan.roster

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class EventDetails(
    val time: LocalTime? = null,
    val location: String? = null,
)

@Serializable
data class Event(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    val guildId: Uuid,
    val date: LocalDate,
    val tables: List<Table> = listOf(),
    val unseated: List<Player> = listOf(),
    val details: EventDetails = EventDetails()
) {
    fun isRegistered(p: Player): Boolean {
        return tables.any { it.isPlayer(p) } || unseated.map { it.id }.contains(p.id)
    }

    fun isHosting(p: Player): Boolean {
        return tables.any { it.isDungeonMaster(p) }
    }

    fun seatedPlayerCount(): Int {
        return tables.sumOf { it.players.size }
    }

    fun playerCount(): Int {
        return seatedPlayerCount() + unseated.size
    }

    fun tableSpace(): Int {
        return tables.sumOf { it.details.playerRange.last }
    }

    fun unclaimedSeatCount(): Int {
        return tableSpace() - seatedPlayerCount()
    }

    private fun remainingCapacity(): Int {
        return tableSpace() - playerCount()
    }

    fun openSeatCount(): Int {
        return maxOf(0, remainingCapacity())
    }

    fun waitingListLength(): Int {
        return 0 - minOf(0, remainingCapacity())
    }

    fun getHostedTable(p: Player): Table? {
        return getHostedTable(p.id)
    }

    fun getHostedTable(dmId: Uuid): Table? {
        return tables.find { it.dungeonMaster.id == dmId }
    }

    fun getFormattedDate(): String {
        return "${date.dayOfWeek.name.substring(0..2)} - ${date.dayOfMonth}. ${
            date.month.name.lowercase().replaceFirstChar { it.titlecase() }
        }, ${date.year}"
    }

    fun getEligibleForSeat(): List<Player> {
        return unseated.take(tableSpace())
    }

    fun getWaitingList(): List<Player> {
        return unseated.takeLast(waitingListLength())
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

@Serializable
data class EventCalendar(
    @Serializable(with = UuidSerializer::class)
    val linkedGuildId: Uuid,
    val events: List<Event>
)