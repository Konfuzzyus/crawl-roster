package org.codecranachan.roster

import discord4j.core.DiscordClient
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.guild.GuildDeleteEvent
import org.slf4j.LoggerFactory

class RosterBot(val core: RosterCore) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val client = Configuration.botToken?.let(DiscordClient::create)

    fun start() {
        if (client == null) {
            logger.info("No bot token - roster bot deactivated")
        } else {
            val gateway = client.login().block()
            if (gateway != null) {
                gateway.on(GuildCreateEvent::class.java).subscribe { event ->
                    logger.info("Butler tending to ${event.guild.id.asString()}")
                    core.guildRoster.link(event.guild)
                }

                gateway.on(GuildDeleteEvent::class.java).subscribe { event ->
                    if (event.isUnavailable) {
                        logger.info("Butler disconnected from ${event.guildId.asString()}")
                    } else {
                        logger.info("Butler abandons ${event.guildId.asString()}")
                    }
                }

                logger.info("Bot connected to discord gateway")
                gateway.onDisconnect().block()
            } else {
                logger.warn("Connection to discord gateway failed.")
            }
        }
    }
}