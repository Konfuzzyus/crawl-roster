package org.codecranachan.roster.query

import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Registration
import org.codecranachan.roster.core.Table

data class ResolvedRegistration(
    val registration: Registration,
    val player: Player,
    val dm: Player?,
    val table: Table?
) {
    val tableDescription: String? =
        when {
            dm == null -> ""
            table == null -> "Waiting for ${dm.discordHandle}"
            else -> "${table.details.adventureTitle ?: "Adventure"} by ${dm.discordHandle}"
        }
}
