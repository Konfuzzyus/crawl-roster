package org.codecranachan.roster.core

import com.benasher44.uuid.Uuid
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.codecranachan.roster.IntRangeSerializer
import org.codecranachan.roster.UuidSerializer

@Serializable
data class Table(
    @Serializable(with = UuidSerializer::class)
    val eventId: Uuid,
    @Serializable(with = UuidSerializer::class)
    val dungeonMasterId: Uuid,
    val details: Details = Details()
) {
    @Serializable
    data class Details(
        val adventureTitle: String? = null,
        val adventureDescription: String? = null,
        val moduleDesignation: String? = null,
        val language: TableLanguage = TableLanguage.English,
        @Serializable(with = IntRangeSerializer::class)
        val playerRange: IntRange = 3..7,
        @Serializable(with = IntRangeSerializer::class)
        val levelRange: IntRange = 1..4,
        val audience: Audience = Audience.Regular,
        val gameSystem: String? = null
    )

    @Transient
    val description: String = details.adventureDescription ?: "The DM did not provide a detailed description."

    @Transient
    val title: String = listOfNotNull(
        details.adventureTitle ?: "Mystery adventure",
        details.moduleDesignation?.let { "($it)" }).joinToString(" ")

    @Transient
    val settings: String =
        "${details.language.name} - Character levels ${details.levelRange.first} to ${details.levelRange.last}"
}


