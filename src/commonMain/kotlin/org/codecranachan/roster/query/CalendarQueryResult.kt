package org.codecranachan.roster.query

import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable
import org.codecranachan.roster.UuidSerializer

@Serializable
data class CalendarQueryResult(
    @Serializable(with = UuidSerializer::class)
    val linkedGuildId: Uuid,
    val events: List<EventQueryResult>
)