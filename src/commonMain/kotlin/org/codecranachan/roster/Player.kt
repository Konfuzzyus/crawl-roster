package org.codecranachan.roster

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    val name: String,
    val discordHandle: String? = null,
    val avatarUrl: String? = null
) {
    fun getAlias(): String {
        return discordHandle ?: name
    }
}
