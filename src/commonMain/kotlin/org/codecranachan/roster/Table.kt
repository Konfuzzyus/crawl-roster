package org.codecranachan.roster

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class TableDetails(
    val adventureTitle: String? = null,
    val adventureDescription: String? = null,
    val moduleDesignation: String? = null,
    val language: TableLanguage = TableLanguage.SwissGerman,
    @Serializable(with = IntRangeSerializer::class)
    val playerRange: IntRange = 3..7,
    @Serializable(with = IntRangeSerializer::class)
    val levelRange: IntRange = 1..4
)

@Serializable
data class Table(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    val dungeonMaster: Player,
    val details: TableDetails = TableDetails()
) {
    fun getName(): String = "${dungeonMaster.getAlias()}'s Table"
}
