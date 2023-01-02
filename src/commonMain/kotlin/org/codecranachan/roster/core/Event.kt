package org.codecranachan.roster.core

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import org.codecranachan.roster.UuidSerializer

@Serializable
data class Event(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    val guildId: Uuid,
    val date: LocalDate,
    val details: Details = Details()
) {
    @Serializable
    data class Details(
        val time: LocalTime? = null,
        val location: String? = null,
        val closedOn: Instant? = null
    )

    val formattedDate: String =
        "${date.dayOfWeek.name.substring(0..2)} - ${date.dayOfMonth}. ${
            date.month.name.lowercase().replaceFirstChar { it.titlecase() }
        }, ${date.year}"
}