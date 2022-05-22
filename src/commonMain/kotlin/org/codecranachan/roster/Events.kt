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
    val guildId: Uuid = uuid4(),
    val date: LocalDate,
    val registeredPlayers: List<Player> = listOf(),
    val hostedTables: List<PlayTable> = listOf()
)

@Serializable
data class EventRegistration(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    val eventId: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    val playerId: Uuid = uuid4()
)
