package org.codecranachan.roster.bot

import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.guild.GuildDeleteEvent
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.Button
import discord4j.core.`object`.entity.User
import discord4j.discordjson.json.InteractionApplicationCommandCallbackData
import discord4j.discordjson.json.InteractionResponseData
import kotlinx.datetime.toJavaLocalDate
import org.codecranachan.roster.DiscordUser
import org.codecranachan.roster.core.Registration
import org.codecranachan.roster.core.RosterCore
import org.codecranachan.roster.core.RosterLogicException
import org.codecranachan.roster.core.events.EventBus
import org.codecranachan.roster.core.events.RosterEvent
import org.codecranachan.roster.query.EventQueryResult
import org.codecranachan.roster.query.ResolvedTable
import org.codecranachan.roster.util.orNull
import org.slf4j.LoggerFactory
import reactor.core.Disposable
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.SignStyle
import java.time.temporal.ChronoField

fun User.asDiscordUser(): DiscordUser =
    DiscordUser(
        id.asString(),
        username,
        avatarUrl,
        discriminator
    )

fun InteractionCreateEvent.sendEphemeralResponse(content: String) {
    val ephemeralFlag = 1 shl 6
    val channelMessageWithSource = 4
    client.restClient.interactionService.createInteractionResponse(
        interaction.id.asLong(),
        interaction.token,
        InteractionResponseData.builder()
            .type(channelMessageWithSource)
            .data(
                InteractionApplicationCommandCallbackData.builder()
                    .content(content)
                    .flags(ephemeralFlag)
                    .build()
            )
            .build()
    ).subscribe()
}


class RosterBot(val core: RosterCore, botToken: String, val rootUrl: String) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val client: DiscordClient = botToken.let(DiscordClient::create)
    private val disposables = ArrayList<Disposable>()

    private val tracking = GuildTracking()

    fun start() {
        val gateway = client.login().block()
        if (gateway != null) {
            logger.info("Bot connected to discord gateway")
            subscribeHandlers(gateway, core.eventBus)
            gateway.onDisconnect().block()
            disposables.forEach(Disposable::dispose)
            logger.info("Bot disconnected from discord gateway")
        } else {
            logger.warn("Connection to discord gateway failed.")
        }
    }

    private fun subscribeHandlers(gateway: GatewayDiscordClient, eventBus: EventBus) {
        eventBus.getFlux().subscribe(this::handleRosterEvent).apply(disposables::add)

        gateway.on(GuildCreateEvent::class.java).subscribe(this::handleGuildCreateEvent).apply(disposables::add)
        gateway.on(GuildDeleteEvent::class.java).subscribe(this::handleGuildDeleteEvent).apply(disposables::add)

        gateway.on(InteractionCreateEvent::class.java).subscribe(this::handleInteraction).apply(disposables::add)

        tracking.subscribeHandlers(gateway).apply(disposables::addAll)
    }


    private fun handleGuildCreateEvent(event: GuildCreateEvent) {
        val link = core.guildRoster.link(event.guild)
        if (link == null) {
            logger.info("Butler unable to tend to ${event.guild.id.asString()} - guild limit reached")
        } else {
            logger.info("Butler tending to ${event.guild.id.asString()}")
            tracking.add(link, event.guild)
        }
    }

    private fun handleGuildDeleteEvent(event: GuildDeleteEvent) {
        if (event.isUnavailable) {
            logger.info("Butler disconnected from ${event.guildId.asString()}")
        } else {
            logger.info("Butler removed from ${event.guildId.asString()}")
        }
        tracking.remove(event.guildId)
    }

    private fun handleInteraction(event: InteractionCreateEvent) {
        event.interaction.commandInteraction.ifPresent {
            val activeId = it.customId.orNull()?.let(ActiveId::fromCustomId)
            when (activeId?.action) {
                Action.RegisterPlayer -> {
                    val p = core.playerRoster.registerDiscordPlayer(event.interaction.user.asDiscordUser())
                    try {
                        core.eventCalendar.registerPlayer(
                            activeId.getParam(0)!!,
                            p.player.id,
                            Registration.Details(activeId.getParam(1)!!)
                        )
                        event.sendEphemeralResponse("I have added you to the waiting list.")
                    } catch (e: RosterLogicException) {
                        event.sendEphemeralResponse(
                            """
                            I am afraid I could not add you to the waiting list.
                            The computer said: `${e.message}`""".trimIndent()
                        )
                    }
                }
                Action.UnregisterPlayer -> {
                    val p = core.playerRoster.registerDiscordPlayer(event.interaction.user.asDiscordUser())
                    try {
                        core.eventCalendar.unregisterPlayer(activeId.getParam(0)!!, p.player.id)
                        event.sendEphemeralResponse("I have canceled your registration. I Hope you can make it next time.")
                    } catch (e: RosterLogicException) {
                        event.sendEphemeralResponse(
                            """
                            I am afraid I could not cancel your registration.
                            The computer said: `${e.message}`""".trimIndent()
                        )
                    }
                }
                else -> {}
            }
        }

    }


    private fun handleRosterEvent(e: RosterEvent) {
        logger.debug("Handling event from RosterCore: $e")
        // TODO: Handle Events
        /*
        when (e) {
            is CalendarEventCreated -> publishEventOnDiscord(e.event)
            is CalendarEventUpdated -> publishEventOnDiscord(e.event)
            is TableCanceled -> publishCanceledTable(e.event, e.table)
            else -> {}
        }
        */
    }

    private fun publishCanceledTable(event: EventQueryResult, table: ResolvedTable) {
        publishEventOnDiscord(event, listOf(table))
    }

    private fun publishEventOnDiscord(query: EventQueryResult, canceledTables: List<ResolvedTable> = emptyList()) {
        tracking.get(query.event.guildId)?.apply {
            withEventChannel(query) { channel ->
                withPinnedEventMessage(channel, query) { msg ->
                    val content = MessageTemplates.eventMessageContent(query)
                    val components =
                        ActionRow.of(
                            Button.primary(ActiveId(Action.RegisterPlayer, query.event.id).asCustomId(), "Sign Up"),
                            Button.primary(ActiveId(Action.UnregisterPlayer, query.event.id).asCustomId(), "Cancel")
                        )

                    val botMessage = BotMessage(botId, query.getChannelName(), content)

                    msg.edit()
                        .withContentOrNull(botMessage.asContent())
                        .withComponents(components)
                        .subscribe()
                }
                query.tables.values.forEach { tbl ->
                    withPinnedTableMessage(channel, tbl) { msg ->
                        val content = MessageTemplates.openTableMessageContent(tbl)

                        val components =
                            ActionRow.of(
                                Button.primary(
                                    ActiveId(
                                        Action.RegisterPlayer,
                                        query.event.id,
                                        tbl.table!!.dungeonMasterId
                                    ).asCustomId(),
                                    "Join table"
                                )
                            )

                        val botMessage = BotMessage(botId, tbl.name, content)

                        msg.edit()
                            .withContentOrNull(botMessage.asContent())
                            .withComponents(components)
                            .subscribe()
                    }
                }
                canceledTables.forEach { tbl ->
                    withPinnedTableMessage(channel, tbl) { msg ->
                        val content = MessageTemplates.closedTableMessageContent(tbl)
                        val botMessage = BotMessage(botId, tbl.name, content)
                        msg.edit()
                            .withContentOrNull(botMessage.asContent())
                            .withComponents()
                            .subscribe()
                    }
                }
            }
        }
    }
}

private val EVENT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendValue(ChronoField.DAY_OF_MONTH, 2)
    .appendLiteral('-')
    .appendValue(ChronoField.MONTH_OF_YEAR, 2)
    .appendLiteral('-')
    .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
    .toFormatter()

fun EventQueryResult.getChannelName(): String {
    return EVENT_DATE_FORMATTER.format(event.date.toJavaLocalDate())
}

fun EventQueryResult.getChannelTopic(): String {
    return "Role playing event on ${event.formattedDate} posted on Crawl Roster"
}