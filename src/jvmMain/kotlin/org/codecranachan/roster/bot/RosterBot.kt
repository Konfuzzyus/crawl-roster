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
import org.codecranachan.roster.Event
import org.codecranachan.roster.Table
import org.codecranachan.roster.bot.ActiveId
import org.codecranachan.roster.logic.CalendarLogicException
import org.codecranachan.roster.logic.RosterCore
import org.codecranachan.roster.logic.events.CalendarEventCreated
import org.codecranachan.roster.logic.events.CalendarEventUpdated
import org.codecranachan.roster.logic.events.EventBus
import org.codecranachan.roster.logic.events.RosterEvent
import org.codecranachan.roster.logic.events.TableCanceled
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
                        core.eventCalendar.registerPlayer(activeId.getParam(0)!!, p.id, activeId.getParam(1))
                        event.sendEphemeralResponse("I have added you to the waiting list.")
                    } catch (e: CalendarLogicException) {
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
                        core.eventCalendar.unregisterPlayer(activeId.getParam(0)!!, p.id)
                        event.sendEphemeralResponse("I have canceled your registration. I Hope you can make it next time.")
                    } catch (e: CalendarLogicException) {
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
        when (e) {
            is CalendarEventCreated -> publishEventOnDiscord(e.event)
            is CalendarEventUpdated -> publishEventOnDiscord(e.event)
            is TableCanceled -> publishCanceledTable(e.event, e.table)
            else -> {}
        }
    }

    private fun publishCanceledTable(event: Event, table: Table) {
        publishEventOnDiscord(event, listOf(table))
    }

    private fun publishEventOnDiscord(event: Event, canceledTables: List<Table> = emptyList()) {
        tracking.get(event.guildId)?.apply {
            withEventChannel(event) { channel ->
                withPinnedEventMessage(channel, event) { msg ->
                    val memberMention = getMemberRole()?.mention ?: ""
                    val location = event.details.location ?: "the usual place"
                    val time = event.details.time ?: "the usual time"

                    val eventStats =
                        "We have ${event.playerCount()} player${if (event.playerCount() == 1) "" else "s"} attending and enough tables for ${event.tableSpace()}"

                    val unseatedList = if (event.getEligibleForSeat().isEmpty()) "" else
                        "\n**Eligible for a table seat**\n${
                            event.getEligibleForSeat().map { "- ${it.asDiscordMention()}" }.joinToString("\n")
                        }"

                    val waitingList = if (event.getWaitingList().isEmpty()) "" else
                        "\n**Waiting List**\n${
                            event.getWaitingList().map { "- ${it.asDiscordMention()}" }.joinToString("\n")
                        }"

                    val text = """
                    $memberMention
                    The event on ${event.getFormattedDate()} is now accepting registrations.
                    Tables will be set at $location and doors will open at $time.                
                    To register you can use the buttons on this message or head over to $rootUrl.
                           
                    --- $eventStats ---
                    """.trimIndent()

                    val seating = "$unseatedList$waitingList"

                    val components =
                        ActionRow.of(
                            Button.primary(ActiveId(Action.RegisterPlayer, event.id).asCustomId(), "Sign Up"),
                            Button.primary(ActiveId(Action.UnregisterPlayer, event.id).asCustomId(), "Cancel")
                        )

                    val botMessage = BotMessage(botId, event.getChannelName(), text + seating)

                    msg.edit()
                        .withContentOrNull(botMessage.asContent())
                        .withComponents(components)
                        .subscribe()
                }
                event.tables.forEach { tbl ->
                    withPinnedTableMessage(channel, tbl) { msg ->
                        val playerList =
                            tbl.players.map { "**Players**\n- ${it.asDiscordMention()}" }.joinToString("\n")

                        val content = listOfNotNull(
                            "Dungeon Master: ${tbl.dungeonMaster.asDiscordMention()}",
                            tbl.details.adventureTitle?.let { "Adventure: $it (${tbl.details.moduleDesignation ?: "Homebrew"})" },
                            "Player Level: ${tbl.details.levelRange.first} to ${tbl.details.levelRange.last}",
                            "Player Limit: ${tbl.details.playerRange.last}",
                            "Language: ${tbl.details.language.flag} ${tbl.details.language.name}",
                            tbl.details.adventureDescription,
                            "",
                            "Join this table using the button below. If you already joined another table you will automatically be removed from the other table.",
                            playerList
                        )

                        val components =
                            ActionRow.of(
                                Button.primary(
                                    ActiveId(Action.RegisterPlayer, event.id, tbl.id).asCustomId(),
                                    "Join table"
                                )
                            )

                        val botMessage = BotMessage(
                            botId,
                            tbl.getName(),
                            content.joinToString("\n")
                        )

                        msg.edit()
                            .withContentOrNull(botMessage.asContent())
                            .withComponents(components)
                            .subscribe()
                    }
                }
                canceledTables.forEach { tbl ->
                    withPinnedTableMessage(channel, tbl) { msg ->
                        val content = listOfNotNull(
                            "${tbl.dungeonMaster.asDiscordMention()} has canceled this table.",
                            "Do not fret, I have moved you back to the registration list and you are free to join another table of your liking."
                        )
                        val botMessage = BotMessage(
                            botId,
                            tbl.getName(),
                            content.joinToString("\n")
                        )
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

fun Event.getChannelName(): String {
    return EVENT_DATE_FORMATTER.format(date.toJavaLocalDate())
}

fun Event.getChannelTopic(): String {
    return "Role playing event on ${getFormattedDate()} posted on Crawl Roster"
}