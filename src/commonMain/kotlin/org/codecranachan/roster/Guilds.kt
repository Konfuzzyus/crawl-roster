package org.codecranachan.roster

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class GuildRoster(
    val guildLimit : Int = 0,
    val linkedGuilds: List<LinkedGuild> = emptyList()
)

@Serializable
data class LinkedGuild(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    val name: String,
    val discordId: String
)