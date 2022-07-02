package org.codecranachan.roster

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class Server(
    val guildLimit : Int = 0,
    val guilds: List<Guild> = emptyList()
)

@Serializable
data class Guild(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    val name: String,
    val discordId: String
)