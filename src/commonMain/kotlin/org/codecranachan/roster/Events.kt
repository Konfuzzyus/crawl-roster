package org.codecranachan.roster

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

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

    fun tableCount(): Int {
        return roster.keys.filterNotNull().size
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