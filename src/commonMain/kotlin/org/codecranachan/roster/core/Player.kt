package org.codecranachan.roster.core

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.codecranachan.roster.UuidSerializer

@Serializable
data class Player(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    val discordId: String,
    val discordHandle: String,
    val avatarUrl: String? = null,
    val details: Details = Details()
) {
    @Transient
    val discordMention: String = listOfNotNull("<@${discordId}>", details.name).joinToString(" ")

    @Transient
    val websiteMention: String = listOfNotNull(discordHandle, details.name?.let { "($it)" }).joinToString(" ")

    @Serializable
    data class Details(
        val name: String? = null,
        val languages: List<TableLanguage> = listOf(TableLanguage.English),
        val playTier: Int = 0
    )
}

