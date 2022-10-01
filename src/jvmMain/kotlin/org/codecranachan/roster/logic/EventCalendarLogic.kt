package org.codecranachan.roster.logic

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import org.codecranachan.roster.Event
import org.codecranachan.roster.EventCalendar
import org.codecranachan.roster.EventDetails
import org.codecranachan.roster.EventRegistration
import org.codecranachan.roster.Table
import org.codecranachan.roster.TableDetails
import org.codecranachan.roster.TableHosting
import org.codecranachan.roster.logic.events.CalendarEventCreated
import org.codecranachan.roster.logic.events.CalendarEventUpdated
import org.codecranachan.roster.logic.events.EventBus
import org.codecranachan.roster.logic.events.TableCanceled
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CalendarLogicException(message: String) : RuntimeException(message)

class EventCalendarLogic(
    private val eventBus: EventBus,
    private val eventRepository: EventRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun getEvent(eventId: Uuid): Event? {
        return eventRepository.getEvent(eventId)
    }

    fun get(linkedGuildId: Uuid): EventCalendar {
        return EventCalendar(
            linkedGuildId,
            eventRepository.getEventsByGuild(linkedGuildId)
        )
    }

    fun addEvent(linkedGuildId: Uuid, event: Event) {
        val e = event.copy(guildId = linkedGuildId)
        eventRepository.addEvent(e)
        eventBus.publish(CalendarEventCreated(e))
    }

    fun updateEvent(eventId: Uuid, details: EventDetails) {
        eventRepository.updateEvent(eventId, details)
        publishEventChange(eventId)
    }

    /**
     * Registers a player for a given event.
     */
    @kotlin.jvm.Throws(CalendarLogicException::class)
    fun registerPlayer(eventId: Uuid, playerId: Uuid, tableId: Uuid?) {
        if (eventRepository.isDungeonMasterForEvent(playerId, eventId)) {
            throw CalendarLogicException("Player is already hosting a table")
        } else {
            if (eventRepository.getRegistration(eventId, playerId) == null) {
                eventRepository.addRegistration(EventRegistration(uuid4(), eventId, playerId, tableId))
                logger.info("Registered $playerId as player with $eventId")
                publishEventChange(eventId)
            } else {
                throw CalendarLogicException("Player is already registered")
            }
        }
    }

    fun updatePlayerRegistration(eventId: Uuid, playerId: Uuid, tableId: Uuid?) {
        if (eventRepository.isPlayerForEvent(playerId, eventId)) {
            eventRepository.updateRegistration(eventId, playerId, tableId)
            publishEventChange(eventId)
        }
    }

    fun unregisterPlayer(eventId: Uuid, playerId: Uuid) {
        if (eventRepository.isPlayerForEvent(playerId, eventId)) {
            eventRepository.deleteRegistration(eventId, playerId)
            publishEventChange(eventId)
        } else {
            throw CalendarLogicException("Player is not registered")
        }
    }

    fun getTable(tableId: Uuid): Table? {
        return eventRepository.getHosting(tableId)
    }

    fun hostTable(hosting: TableHosting) {
        if (!eventRepository.isPlayerForEvent(hosting.dungeonMasterId, hosting.eventId)) {
            eventRepository.addHosting(hosting)
            logger.info("Registered ${hosting.dungeonMasterId} as dungeon master with ${hosting.eventId}")
            publishEventChange(hosting.eventId)
        }
    }

    fun updateTable(tableId: Uuid, details: TableDetails) {
        eventRepository.updateHosting(tableId, details)
    }

    fun cancelTable(eventId: Uuid, dmId: Uuid) {
        eventRepository.getEvent(eventId)?.let {event ->
            val table = event.getHostedTable(dmId)
            if (table != null) {
                eventRepository.deleteHosting(table.id)
                eventBus.publish(TableCanceled(event, table))
            }
        }
    }

    private fun publishEventChange(eventId: Uuid) {
        eventRepository.getEvent(eventId)?.let {
            eventBus.publish(CalendarEventUpdated(it))
        }
    }
}