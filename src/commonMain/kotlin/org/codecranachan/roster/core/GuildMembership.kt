package org.codecranachan.roster.core

import kotlinx.serialization.Serializable
import org.codecranachan.roster.LinkedGuild

@Serializable
data class GuildMembership(
    val linkedGuild: LinkedGuild,
    val isAdmin: Boolean,
    val isDungeonMaster: Boolean
)