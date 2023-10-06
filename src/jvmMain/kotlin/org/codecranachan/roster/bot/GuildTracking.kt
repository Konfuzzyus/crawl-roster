package org.codecranachan.roster.bot

import com.benasher44.uuid.Uuid
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.channel.CategoryCreateEvent
import discord4j.core.event.domain.channel.CategoryDeleteEvent
import discord4j.core.event.domain.channel.CategoryUpdateEvent
import discord4j.core.event.domain.channel.TextChannelCreateEvent
import discord4j.core.event.domain.channel.TextChannelDeleteEvent
import discord4j.core.event.domain.channel.TextChannelUpdateEvent
import discord4j.core.event.domain.role.RoleCreateEvent
import discord4j.core.event.domain.role.RoleDeleteEvent
import discord4j.core.event.domain.role.RoleUpdateEvent
import discord4j.core.`object`.entity.Guild
import org.codecranachan.roster.LinkedGuild
import org.slf4j.LoggerFactory
import reactor.core.Disposable
import java.util.concurrent.ConcurrentHashMap

class GuildTracking {
    private val tendedGuilds = ConcurrentHashMap<Snowflake, GuildTracker>()
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var botId: Snowflake

    fun subscribeHandlers(gateway: GatewayDiscordClient): List<Disposable> {
        botId = gateway.selfId

        val disposables = ArrayList<Disposable>()

        // Categories
        gateway.on(CategoryCreateEvent::class.java).subscribe(this::handleCategoryCreateEvent).apply(disposables::add)
        gateway.on(CategoryUpdateEvent::class.java).subscribe(this::handleCategoryUpdateEvent).apply(disposables::add)
        gateway.on(CategoryDeleteEvent::class.java).subscribe(this::handleCategoryDeleteEvent).apply(disposables::add)

        // Text Channels
        gateway.on(TextChannelCreateEvent::class.java).subscribe(this::handleTextChannelCreateEvent)
            .apply(disposables::add)
        gateway.on(TextChannelUpdateEvent::class.java).subscribe(this::handleTextChannelUpdateEvent)
            .apply(disposables::add)
        gateway.on(TextChannelDeleteEvent::class.java).subscribe(this::handleTextChannelDeleteEvent)
            .apply(disposables::add)

        // Roles
        gateway.on(RoleCreateEvent::class.java).subscribe(this::handleRoleCreateEvent)
            .apply(disposables::add)
        gateway.on(RoleUpdateEvent::class.java).subscribe(this::handleRoleUpdateEvent)
            .apply(disposables::add)
        gateway.on(RoleDeleteEvent::class.java).subscribe(this::handleRoleDeleteEvent)
            .apply(disposables::add)

        return disposables
    }

    fun add(link: LinkedGuild, discordGuild: Guild) {
        val tracker = GuildTracker(link, discordGuild, botId)
        tracker.initialize()
        tendedGuilds[discordGuild.id] = tracker
    }

    fun remove(guildId: Snowflake) {
        tendedGuilds.remove(guildId)
    }

    fun get(linkedGuildId: Uuid): GuildTracker? {
        return tendedGuilds.values.find { t -> t.linkedGuild.id == linkedGuildId }
    }

    private fun removeDislocatedEntity(id: Snowflake) {
        tendedGuilds.forEach { it.value.removeEntity(id) }
    }

    private fun handleCategoryCreateEvent(event: CategoryCreateEvent) {
        logger.debug("Category created: ${event.category.name}")
        tendedGuilds[event.category.guildId]?.putEntity(event.category)
    }

    private fun handleCategoryUpdateEvent(event: CategoryUpdateEvent) {
        logger.debug("Category updated: ${event.current.name}")
        tendedGuilds[event.current.guildId]?.putEntity(event.current)
    }

    private fun handleCategoryDeleteEvent(event: CategoryDeleteEvent) {
        logger.debug("Category deleted: ${event.category.name}")
        tendedGuilds[event.category.guildId]?.removeEntity(event.category)
    }

    private fun handleTextChannelCreateEvent(event: TextChannelCreateEvent) {
        logger.debug("Text channel created: ${event.channel.name}")
        tendedGuilds[event.channel.guildId]?.putEntity(event.channel)
    }

    private fun handleTextChannelUpdateEvent(event: TextChannelUpdateEvent) {
        logger.debug("Text channel updated: ${event.current.name}")
        tendedGuilds[event.current.guildId]?.putEntity(event.current)
    }

    private fun handleTextChannelDeleteEvent(event: TextChannelDeleteEvent) {
        logger.debug("Text channel deleted: ${event.channel.name}")
        tendedGuilds[event.channel.guildId]?.removeEntity(event.channel)
    }

    private fun handleRoleCreateEvent(event: RoleCreateEvent) {
        logger.debug("Role created: ${event.role.name}")
        tendedGuilds[event.role.guildId]?.putEntity(event.role)
    }

    private fun handleRoleUpdateEvent(event: RoleUpdateEvent) {
        logger.debug("Role created: ${event.current.name}")
        tendedGuilds[event.current.guildId]?.putEntity(event.current)
    }

    private fun handleRoleDeleteEvent(event: RoleDeleteEvent) {
        logger.debug("Role deleted: ${event.roleId}")
        removeDislocatedEntity(event.roleId)
    }

}

