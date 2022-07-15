package org.codecranachan.roster.logic

import com.benasher44.uuid.Uuid
import org.codecranachan.roster.GuildMembership
import org.codecranachan.roster.Player
import org.codecranachan.roster.PlayerDetails

interface PlayerRepository {
    fun getPlayer(playerId: Uuid): Player?
    fun getPlayerByDiscordId(discordId: String): Player?
    fun addPlayer(player: Player)
    fun updatePlayer(playerId: Uuid, details: PlayerDetails)
    fun getGuildMemberships(playerId: Uuid): List<GuildMembership>
    fun setGuildMembership(playerId: Uuid, guildId: Uuid, isAdmin: Boolean, isDungeonMaster: Boolean)
    fun isGuildAdmin(playerId: Uuid, linkedGuildId: Uuid): Boolean
    fun isGuildDm(playerId: Uuid, linkedGuildId: Uuid): Boolean
}