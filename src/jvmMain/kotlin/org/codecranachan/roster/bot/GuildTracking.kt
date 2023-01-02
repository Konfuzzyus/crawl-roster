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
import discord4j.core.`object`.PermissionOverwrite
import discord4j.core.`object`.entity.Entity
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.channel.Category
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import org.codecranachan.roster.LinkedGuild
import org.codecranachan.roster.query.EventQueryResult
import org.codecranachan.roster.query.ResolvedTable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.Disposable
import reactor.core.publisher.Mono
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

class GuildTracker(
    val linkedGuild: LinkedGuild,
    val discordGuild: Guild,
    val botId: Snowflake
) {
    companion object {
        const val eventCalendarCategoryName = "Organisation"
        const val dungeonMasterRoleName = "DM"
        const val memberRoleName = "Member"
    }

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val roles = HashMap<Snowflake, Role>()
    private val categories = HashMap<Snowflake, Category>()
    private val textChannels = HashMap<Snowflake, TextChannel>()

    fun initialize() {
        discordGuild.channels.subscribe { c -> putEntity(c) }
        discordGuild.roles.subscribe { r -> putEntity(r) }
    }

    fun putEntity(e: Entity) {
        when (e) {
            is TextChannel -> textChannels[e.id] = e
            is Category -> categories[e.id] = e
            is Role -> roles[e.id] = e
        }
    }

    fun removeEntity(e: Snowflake) {
        textChannels.remove(e)
        categories.remove(e)
        roles.remove(e)
    }

    fun removeEntity(e: Entity) {
        when (e) {
            is TextChannel -> textChannels.remove(e.id)
            is Category -> categories.remove(e.id)
            is Role -> roles.remove(e.id)
        }
    }

    fun getEventCalendarCategory(): Category? {
        return categories.values.find { c -> c.name.equals(eventCalendarCategoryName, ignoreCase = true) }
    }

    fun getEventChannel(event: EventQueryResult, categoryChannel: Category? = getEventCalendarCategory()): TextChannel? {
        return categoryChannel?.let { category ->
            textChannels.values
                .filter { c -> category.id.equals(c.categoryId.orElse(null)) }
                .find { c -> c.name.equals(event.getChannelName()) }
        }
    }

    fun getEveryoneRole(): Role? {
        return roles.values.find { it.isEveryone }
    }

    fun getMemberRole(): Role? {
        return roles.values.find { it.name == memberRoleName }
    }

    fun getDungeonMasterRole(): Role? {
        return roles.values.find { it.name == dungeonMasterRoleName }
    }

    fun withEventCalendarCategory(block: (Category) -> Unit) {
        Mono.justOrEmpty<Category>(getEventCalendarCategory())
            .switchIfEmpty(
                discordGuild.createCategory(eventCalendarCategoryName)
                    .withReason("Guild is missing $eventCalendarCategoryName category to post Crawl Roster events")
            )
            .subscribe(block)
    }

    fun withEventChannel(query: EventQueryResult, block: (TextChannel) -> Unit) {
        withEventCalendarCategory { category ->
            Mono.justOrEmpty<TextChannel>(getEventChannel(query, category))
                .switchIfEmpty(
                    discordGuild.createTextChannel(query.getChannelName())
                        .withTopic(query.getChannelTopic())
                        .withPosition(query.event.date.toEpochDays())
                        .withParentId(category.id)
                        .withPermissionOverwrites(membersOnly(Permission.VIEW_CHANNEL, Permission.SEND_MESSAGES))
                        .withReason("New event posted by Crawl Roster")
                )
                .subscribe(block)
        }
    }

    fun withPinnedEventMessage(eventChannel: TextChannel, event: EventQueryResult, block: (Message) -> Unit) {
        withPinnedBotMessage(eventChannel, event.getChannelName(), block)
    }

    fun withPinnedTableMessage(eventChannel: TextChannel, table: ResolvedTable, block: (Message) -> Unit) {
        withPinnedBotMessage(eventChannel, table.name, block)
    }

    private fun withPinnedBotMessage(
        channel: GuildMessageChannel,
        title: String,
        block: (Message) -> Unit
    ) {
        val expected = BotMessage(botId, title)

        channel.pinnedMessages
            .filter { msg -> expected.hasSameAuthor(msg) && expected.hasSameTitle(msg) }
            .take(1)
            .switchIfEmpty { sub ->
                channel.createMessage(expected.asContent()).doOnNext {
                    it.pin().subscribe()
                }.subscribe(sub)
            }.subscribe(block)
    }

    private fun membersOnly(vararg permissions: Permission): List<PermissionOverwrite> {
        val everyone = getEveryoneRole()
        val member = getMemberRole()
        if (everyone == null || member == null) {
            return emptyList()
        } else {
            val set = PermissionSet.of(*permissions)
            return listOf(
                PermissionOverwrite.forRole(everyone.id, PermissionSet.none(), set),
                PermissionOverwrite.forRole(member.id, set, PermissionSet.none()),
                PermissionOverwrite.forMember(botId, set, PermissionSet.none())
            )
        }
    }
}