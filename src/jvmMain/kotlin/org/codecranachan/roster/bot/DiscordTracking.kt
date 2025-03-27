package org.codecranachan.roster.bot

import com.benasher44.uuid.Uuid
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import discord4j.core.event.domain.channel.CategoryCreateEvent
import discord4j.core.event.domain.channel.CategoryDeleteEvent
import discord4j.core.event.domain.channel.CategoryUpdateEvent
import discord4j.core.event.domain.channel.TextChannelCreateEvent
import discord4j.core.event.domain.channel.TextChannelDeleteEvent
import discord4j.core.event.domain.channel.TextChannelUpdateEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.MessageDeleteEvent
import discord4j.core.event.domain.message.MessageUpdateEvent
import discord4j.core.event.domain.role.RoleCreateEvent
import discord4j.core.event.domain.role.RoleDeleteEvent
import discord4j.core.event.domain.role.RoleUpdateEvent
import discord4j.core.event.domain.thread.ThreadChannelCreateEvent
import discord4j.core.event.domain.thread.ThreadChannelDeleteEvent
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.ThreadChannel
import org.codecranachan.roster.LinkedGuild
import org.slf4j.LoggerFactory
import reactor.core.Disposable
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

class DiscordTracking {
    private val tendedGuilds = ConcurrentHashMap<Snowflake, GuildTracker>()
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var botId: Snowflake

    fun subscribeHandlers(gateway: GatewayDiscordClient): List<Disposable> {
        botId = gateway.selfId

        val disposables = ArrayList<Disposable>()

        // Categories
        gateway.on(CategoryDeleteEvent::class.java).subscribe(this::handleCategoryDeleteEvent).apply(disposables::add)

        // Text Channels
        gateway.on(TextChannelDeleteEvent::class.java).subscribe(this::handleTextChannelDeleteEvent)
            .apply(disposables::add)

        // Text Messages
        gateway.on(MessageDeleteEvent::class.java).subscribe(this::handleMessageDeleteEvent)
            .apply(disposables::add)

        // Thread Channels
        gateway.on(ThreadChannelDeleteEvent::class.java).subscribe(this::handleThreadChannelDeleteEvent)
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

    private fun handleCategoryDeleteEvent(event: CategoryDeleteEvent) {
        logger.debug("Category deleted: {}", event.category.name)
        tendedGuilds[event.category.guildId]?.removeEntity(event.category.id)
    }

    private fun handleTextChannelDeleteEvent(event: TextChannelDeleteEvent) {
        logger.debug("Text channel deleted {}", event.channel.name)
        tendedGuilds[event.channel.guildId]?.apply {
            removeEntity(event.channel.id)
            removeEntities(Message::class.java) { it.channelId == event.channel.id }
            getEntities(ThreadChannel::class.java)
                .filterValues { it.parentId.getOrNull() == event.channel.id }
                .forEach { (_, thread) ->
                    removeEntity(thread.id)
                    removeEntities(Message::class.java) { it.channelId == thread.id }
                }
        }
    }

    private fun handleThreadChannelDeleteEvent(event: ThreadChannelDeleteEvent) {
        logger.debug("Thread channel deleted {}", event.channel.id)
        tendedGuilds[event.channel.guildId]?.apply {
            removeEntity(event.channel.id)
            removeEntities(Message::class.java) { it.channelId == event.channel.id }
        }
    }

    private fun handleMessageDeleteEvent(event: MessageDeleteEvent) {
        logger.debug("Message deleted: {}", event.messageId)
        event.guildId.getOrNull()?.apply { tendedGuilds[this]?.removeEntity(event.messageId) }
    }

    private fun handleRoleCreateEvent(event: RoleCreateEvent) {
        logger.debug("Role created: {}", event.role.name)
        tendedGuilds[event.role.guildId]?.putEntity(event.role)
    }

    private fun handleRoleUpdateEvent(event: RoleUpdateEvent) {
        logger.debug("Role updated: {}", event.current.name)
        tendedGuilds[event.current.guildId]?.putEntity(event.current)
    }

    private fun handleRoleDeleteEvent(event: RoleDeleteEvent) {
        logger.debug("Role deleted: {}", event.roleId)
        tendedGuilds[event.guildId]?.removeEntity(event.roleId)
    }

}

