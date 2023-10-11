package org.codecranachan.roster.bot

import com.benasher44.uuid.Uuid
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
import org.codecranachan.roster.DiscordUser
import org.codecranachan.roster.core.PlayerAlreadyRegistered
import org.codecranachan.roster.core.Registration
import org.codecranachan.roster.core.RosterCore
import org.codecranachan.roster.core.RosterLogicException
import org.codecranachan.roster.core.events.CalendarEventCanceled
import org.codecranachan.roster.core.events.CalendarEventClosed
import org.codecranachan.roster.core.events.CalendarEventCreated
import org.codecranachan.roster.core.events.CalendarEventUpdated
import org.codecranachan.roster.core.events.EventBus
import org.codecranachan.roster.core.events.RegistrationCanceled
import org.codecranachan.roster.core.events.RegistrationCreated
import org.codecranachan.roster.core.events.RegistrationUpdated
import org.codecranachan.roster.core.events.RosterEvent
import org.codecranachan.roster.core.events.TableCanceled
import org.codecranachan.roster.core.events.TableCreated
import org.codecranachan.roster.core.events.TableUpdated
import org.codecranachan.roster.query.PlayerQueryResult
import org.codecranachan.roster.util.orNull
import org.slf4j.LoggerFactory
import reactor.core.Disposable

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


class RosterBot(val core: RosterCore, botToken: String, rootUrl: String) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val client: DiscordClient = botToken.let(DiscordClient::create)
    private val disposables = ArrayList<Disposable>()

    private val tracking = DiscordTracking()

    private val templates = MessageTemplates(rootUrl)

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

    private fun createPlayerRegistration(
        interaction: InteractionCreateEvent,
        p: PlayerQueryResult,
        eventId: Uuid,
        dmId: Uuid?
    ) {
        core.eventCalendar.addPlayerRegistration(
            eventId,
            p.player.id,
            Registration.Details(dmId)
        )
        if (dmId == null) {
            interaction.sendEphemeralResponse("I have added you to the waiting list.")
        } else {
            interaction.sendEphemeralResponse("I have added you to this table.")
        }
    }

    private fun updatePlayerRegistration(
        interaction: InteractionCreateEvent,
        p: PlayerQueryResult,
        eventId: Uuid,
        dmId: Uuid?
    ) {
        core.eventCalendar.updatePlayerRegistration(
            eventId,
            p.player.id,
            Registration.Details(dmId)
        )
        if (dmId == null) {
            interaction.sendEphemeralResponse("I have moved you to the waiting list.")
        } else {
            interaction.sendEphemeralResponse("I have moved you to this table.")
        }
    }

    private fun handleInteraction(event: InteractionCreateEvent) {
        event.interaction.commandInteraction.ifPresent {
            val activeId = it.customId.orNull()?.let(ActiveId::fromCustomId)
            when (activeId?.action) {
                Action.RegisterPlayer -> {
                    val p = core.playerRoster.registerDiscordPlayer(event.interaction.user.asDiscordUser())
                    try {
                        createPlayerRegistration(event, p, activeId.getParam(0)!!, activeId.getParam(1))
                    } catch (e: RosterLogicException) {
                        when (e) {
                            is PlayerAlreadyRegistered -> {
                                updatePlayerRegistration(event, p, activeId.getParam(0)!!, activeId.getParam(1))
                            }
                            else -> {
                                event.sendEphemeralResponse(
                                    """
                                    I am afraid I could not process your registration.
                                    The computer said: `${e.message}`""".trimIndent()
                                )
                            }
                        }

                    }
                }
                Action.UnregisterPlayer -> {
                    val p = core.playerRoster.registerDiscordPlayer(event.interaction.user.asDiscordUser())
                    try {
                        core.eventCalendar.deletePlayerRegistration(activeId.getParam(0)!!, p.player.id)
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
        when (e) {
            is CalendarEventCreated -> updateEventOnDiscord(e.current.id)
            is CalendarEventUpdated -> updateEventOnDiscord(e.current.id)
            is CalendarEventCanceled -> {}
            is CalendarEventClosed -> {}
            is TableCreated -> {
                updateEventOnDiscord(e.current.eventId)
                updateTableOnDiscord(e.current.eventId, e.current.dungeonMasterId)
            }
            is TableUpdated -> {
                updateEventOnDiscord(e.current.eventId)
                updateTableOnDiscord(e.current.eventId, e.current.dungeonMasterId)
            }
            is TableCanceled -> {
                updateEventOnDiscord(e.previous.eventId)
                cancelTableOnDiscord(e.previous.eventId, e.previous.dungeonMasterId)
            }
            is RegistrationCreated -> {
                updateEventOnDiscord(e.current.eventId)
                e.current.details.dungeonMasterId?.let { dmId ->
                    updateTableOnDiscord(e.current.eventId, dmId)
                }
            }
            is RegistrationUpdated -> {
                updateEventOnDiscord(e.current.eventId)
                e.previous.details.dungeonMasterId?.let { dmId ->
                    updateTableOnDiscord(e.previous.eventId, dmId)
                }
                e.current.details.dungeonMasterId?.let { dmId ->
                    updateTableOnDiscord(e.current.eventId, dmId)
                }
            }
            is RegistrationCanceled -> {
                updateEventOnDiscord(e.previous.eventId)
                e.previous.details.dungeonMasterId?.let { dmId ->
                    updateTableOnDiscord(e.previous.eventId, dmId)
                }
            }
            else -> {}
        }
    }

    private fun updateEventOnDiscord(eventId: Uuid) {
        val data = core.eventCalendar.queryEvent(eventId) ?: return
        tracking.get(data.event.guildId)?.apply {
            withEventMessage(data.event) { msg ->
                val content = templates.eventMessageContent(data)
                val components =
                    ActionRow.of(
                        Button.primary(
                            ActiveId(Action.RegisterPlayer, data.event.id).asCustomId(),
                            "Sign Up"
                        ),
                        Button.primary(
                            ActiveId(Action.UnregisterPlayer, data.event.id).asCustomId(),
                            "Cancel"
                        )
                    )

                val botMessage = BotMessage(botId, data.event.getChannelName(), content)

                msg.edit()
                    .withContentOrNull(botMessage.asContent().take(2000))
                    .withComponents(components)
                    .subscribe()
            }
        }
    }

    private fun updateTableOnDiscord(eventId: Uuid, dmId: Uuid) {
        val data = core.eventCalendar.queryTable(eventId, dmId) ?: return
        tracking.get(data.event.guildId)?.apply {
            withTableMessage(data.event, data.dm) { msg ->
                val content = templates.openTableMessageContent(data)

                val components =
                    ActionRow.of(
                        Button.primary(
                            ActiveId(
                                Action.RegisterPlayer,
                                data.event.id,
                                data.dm.id
                            ).asCustomId(),
                            "Join table"
                        )
                    )

                val botMessage = BotMessage(botId, BotMessage.getTableTitle(data.event, data.dm), content)

                msg.edit()
                    .withContentOrNull(botMessage.asContent().take(2000))
                    .withComponents(components)
                    .subscribe()
            }
        }
    }

    private fun cancelTableOnDiscord(eventId: Uuid, dmId: Uuid) {
        val event = core.eventCalendar.queryEvent(eventId)?.event ?: return
        val dm = core.playerRoster.getPlayer(dmId)?.player ?: return
        tracking.get(event.guildId)?.apply {
            withTableMessage(event, dm) { msg ->
                val content = templates.closedTableMessageContent(dm)
                val botMessage = BotMessage(botId, BotMessage.getTableTitle(event, dm), content)
                msg.edit()
                    .withContentOrNull(botMessage.asContent().take(2000))
                    .withComponents()
                    .subscribe()
            }
        }
    }

}