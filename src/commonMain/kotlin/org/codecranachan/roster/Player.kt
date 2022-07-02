package org.codecranachan.roster

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class PlayerDetails (
    val name: String = "Anonymous",
    val languages: List<TableLanguage> = listOf(TableLanguage.English)
)



@Serializable
data class Player(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    val discordHandle: String,
    val avatarUrl: String? = null,
    val details: PlayerDetails = PlayerDetails()
)