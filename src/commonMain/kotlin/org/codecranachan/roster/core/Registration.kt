package org.codecranachan.roster.core

import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.codecranachan.roster.UuidSerializer

@Serializable
data class Registration(
    @Serializable(with = UuidSerializer::class)
    val eventId: Uuid,
    @Serializable(with = UuidSerializer::class)
    val playerId: Uuid,
    val meta: Metadata = Metadata(),
    val details: Details = Details()
) {
    @Serializable
    data class Metadata(
        val registrationDate: Instant = Clock.System.now()
    )

    @Serializable
    data class Details(
        @Serializable(with = UuidSerializer::class)
        val dungeonMasterId: Uuid? = null
    )
}