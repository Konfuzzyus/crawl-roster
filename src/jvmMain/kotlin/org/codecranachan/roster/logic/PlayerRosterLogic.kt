package org.codecranachan.roster.logic

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import discord4j.discordjson.json.UserGuildData
import discord4j.rest.util.PermissionSet
import org.codecranachan.roster.DiscordUser
import org.codecranachan.roster.Player
import org.codecranachan.roster.PlayerDetails
import org.codecranachan.roster.discord.DiscordApiClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PlayerRosterLogic(
    private val playerRepository: PlayerRepository,
    private val guildRepository: GuildRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun getPlayer(playerId: Uuid): Player? {
        return playerRepository.getPlayer(playerId)?.withGuildMemberships()
    }

    /**
     * Registers a discord user with the server and returns the resulting Player instance.
     * If the discord user is already registered, it returns the existing Player for that user.
     */
    fun registerDiscordPlayer(discordIdentity: DiscordUser): Player {
        val existingPlayer = playerRepository.getPlayerByDiscordId(discordIdentity.id)
        return if (existingPlayer == null) {
            val player = Player(
                uuid4(),
                discordIdentity.id,
                discordIdentity.username,
                discordIdentity.getAvatarUrl(),
                PlayerDetails()
            )
            playerRepository.addPlayer(player)
            player
        } else {
            existingPlayer.withGuildMemberships()
        }
    }

    suspend fun refreshGuildRoles(playerId: Uuid, client: DiscordApiClient) {
        val userGuilds = client.fetchUserGuildInformation().associateBy { it.id }
        guildRepository.getLinkedGuilds().forEach { linkedGuild ->
            val discordGuild = userGuilds[linkedGuild.discordId]
            if (discordGuild != null) {
                // TODO: Figure out a way to determine who is a DM and who is not, for now, everyone is a DM
                val isDungeonMaster = true
                val isAdmin = discordGuild.isAdmin()
                playerRepository.setGuildMembership(playerId, linkedGuild.id, isAdmin, isDungeonMaster)
            }
        }
    }

    fun updatePlayer(playerId: Uuid, details: PlayerDetails) {
        playerRepository.updatePlayer(playerId, details)
    }

    /**
     * Can be used to check whether a particular player is an admin of the requested guild. Use if you need this
     * information and do not have a Player object handy. This is less straining on the Database than fecthing the
     * Player object.
     *
     * @param playerId Uuid of the player
     * @param guildId Uuid of the linked guild to inspect player roles for
     * @return true when the player is an admin of the given guild. Returns false when the player and/or the guild do not exist.
     */
    fun isGuildAdmin(playerId: Uuid, guildId: Uuid): Boolean {
        return getPlayer(playerId)?.isAdminOf(guildId) ?: false
    }


    /**
     * Can be used to check whether a particular player is a dungeon master of the requested guild. Use if you need this
     * information and do not have a Player object handy. This is less straining on the Database than fetching the
     * Player object.
     *
     * @param playerId Uuid of the player
     * @param guildId Uuid of the linked guild to inspect player roles for
     * @return true when the player is an admin of the given guild. Returns false when the player and/or the guild do not exist.
     */
    fun isGuildDm(playerId: Uuid, guildId: Uuid): Boolean {
        return playerRepository.isGuildDm(playerId, guildId)
    }

    /**
     * Returns a copy fo the player instance with guild membership information added.
     */
    private fun Player.withGuildMemberships(): Player {
        return copy(memberships = playerRepository.getGuildMemberships(id))
    }

    private fun UserGuildData.permissionSet(): PermissionSet {
        return if (permissions().isAbsent) {
            PermissionSet.none()
        } else {
            PermissionSet.of(permissions().get())
        }
    }
}