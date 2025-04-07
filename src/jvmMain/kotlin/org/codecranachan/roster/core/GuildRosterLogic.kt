package org.codecranachan.roster.core

import com.benasher44.uuid.uuid4
import discord4j.core.`object`.entity.Guild
import org.codecranachan.roster.BotCoordinates
import org.codecranachan.roster.GuildRoster
import org.codecranachan.roster.LinkedGuild
import org.codecranachan.roster.repo.GuildRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GuildRosterLogic(
    private val guildRepository: GuildRepository,
    private val linkedGuildLimit: Int,
    private val botCoordinates: BotCoordinates?,

    ) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun get(): GuildRoster {
        return GuildRoster(
            linkedGuildLimit,
            guildRepository.getLinkedGuilds(),
            botCoordinates
        )
    }

    /**
     * Links a discord guild with this server.
     */
    fun link(discordGuild: Guild): LinkedGuild? {
        val guilds = guildRepository.getLinkedGuilds()
        val link = guilds.find { it.discordId == discordGuild.id.asString() }
        return if (link != null) {
            if (discordGuild.name != link.name) {
                guildRepository.updateGuild(link.copy(name = discordGuild.name))
            }
            logger.debug("Did not link guild ${discordGuild.name} to the server - already linked")
            link
        } else if (guilds.size >= linkedGuildLimit) {
            logger.info("Did not link guild ${discordGuild.name} to the server - guild limit reached")
            null
        } else {
            val newLink = LinkedGuild(
                uuid4(),
                discordGuild.name,
                discordGuild.id.asString()
            )
            guildRepository.addLinkedGuild(newLink)
            logger.info("Guild ${discordGuild.name} has been linked to the server")
            newLink
        }
    }

    fun update(discordGuild: Guild) {
        val guilds = guildRepository.getLinkedGuilds()
        val link = guilds.find { it.discordId == discordGuild.id.asString() }
        if (link != null && discordGuild.name != link.name) {
            guildRepository.updateGuild(link.copy(name = discordGuild.name))
        }
    }
}