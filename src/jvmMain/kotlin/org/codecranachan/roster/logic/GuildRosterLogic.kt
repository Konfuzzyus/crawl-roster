package org.codecranachan.roster.logic

import com.benasher44.uuid.uuid4
import discord4j.core.`object`.entity.Guild
import org.codecranachan.roster.GuildRoster
import org.codecranachan.roster.LinkedGuild
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GuildRosterLogic(private val guildRepository: GuildRepository, private val linkedGuildLimit: Int) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun get(): GuildRoster {
        return GuildRoster(
            linkedGuildLimit,
            guildRepository.getLinkedGuilds()
        )
    }

    /**
     * Links a discord guild with this server.
     */
    fun link(discordGuild: Guild) {
        val guilds = guildRepository.getLinkedGuilds()
        if (guilds.any { it.discordId == discordGuild.id.asString() }) {
            logger.debug("Did not link guild ${discordGuild.name} to the server - already linked")
        } else if (guilds.size >= linkedGuildLimit) {
            logger.info("Did not link guild ${discordGuild.name} to the server - guild limit reached")
        } else {
            guildRepository.addLinkedGuild(
                LinkedGuild(
                    uuid4(),
                    discordGuild.name,
                    discordGuild.id.asString()
                )
            )
            logger.info("Guild ${discordGuild.name} has been linked to the server")
        }
    }
}