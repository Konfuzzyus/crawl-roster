package org.codecranachan.roster

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class BotCoordinates(
    val clientId: String,
    val botPermissions: Long
) {
    fun getInviteLink() =
        "https://discord.com/api/oauth2/authorize?client_id=${clientId}&scope=bot&permissions=${botPermissions}"
}


@Serializable
data class GuildRoster(
    val guildLimit: Int = 0,
    val linkedGuilds: List<LinkedGuild> = emptyList(),
    val botCoordinates: BotCoordinates? = null
)

@Serializable
data class LinkedGuild(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    val name: String,
    val discordId: String
)