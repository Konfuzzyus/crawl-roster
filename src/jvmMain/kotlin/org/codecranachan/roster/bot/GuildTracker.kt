package org.codecranachan.roster.bot

import discord4j.common.util.Snowflake
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
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Player
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

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

    private val roles = HashMap<Snowflake, Role>()
    private val categories = HashMap<Snowflake, Category>()
    private val textChannels = HashMap<Snowflake, TextChannel>()

    private val categoryMap: ConcurrentHashMap<String, CompletableFuture<Category>> = ConcurrentHashMap()
    private val textChannelMap: ConcurrentHashMap<String, CompletableFuture<TextChannel>> = ConcurrentHashMap()

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

    private fun getEventCalendarCategory(): Mono<Category> {
        return Mono.fromFuture(
            categoryMap.getOrPut(eventCalendarCategoryName) {
                Mono.justOrEmpty<Category>(
                    categories.values.find {
                        it.name.equals(
                            eventCalendarCategoryName,
                            ignoreCase = true
                        )
                    })
                    .switchIfEmpty(
                        discordGuild
                            .createCategory(eventCalendarCategoryName)
                            .withReason("Guild is missing $eventCalendarCategoryName category to post Crawl Roster events")
                    )
                    .toFuture()
            })
    }

    private fun getEventChannel(event: Event): Mono<TextChannel> {
        return Mono.fromFuture(textChannelMap.getOrPut(event.getChannelName()) {
            getEventCalendarCategory()
                .flatMap { category ->
                    Mono.justOrEmpty(
                        textChannels.values
                            .filter { c -> category.id.equals(c.categoryId.orElse(null)) }
                            .find { c -> c.name.equals(event.getChannelName()) }
                    )
                        .switchIfEmpty(
                            discordGuild
                                .createTextChannel(event.getChannelName())
                                .withTopic(event.getChannelTopic())
                                .withPosition(event.date.toEpochDays())
                                .withParentId(category.id)
                                .withPermissionOverwrites(
                                    membersOnly(
                                        Permission.VIEW_CHANNEL,
                                        Permission.SEND_MESSAGES
                                    )
                                )
                                .withReason("New event posted by Crawl Roster")
                        )
                }
                .map { it!! }
                .toFuture()
        })
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

    fun withEventMessage(event: Event, block: (Message) -> Unit) {
        getEventChannel(event).subscribe { eventChannel ->
            withPinnedBotMessage(eventChannel, event.getChannelName(), block)
        }
    }

    fun withTableMessage(event: Event, dm: Player, block: (Message) -> Unit) {
        getEventChannel(event).subscribe { eventChannel ->
            withPinnedBotMessage(eventChannel, dm.getTableName(), block)
        }
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
            .switchIfEmpty(
                channel.createMessage(expected.asContent()).doOnNext { it.pin().subscribe() }
            ).subscribe(block)
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