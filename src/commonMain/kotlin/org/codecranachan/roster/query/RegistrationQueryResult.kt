package org.codecranachan.roster.query

import kotlinx.serialization.Serializable
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Registration

@Serializable
data class RegistrationQueryResult(
    val registration: Registration,
    val event: Event,
    val player: Player,
    val dungeonMaster: Player?
)

