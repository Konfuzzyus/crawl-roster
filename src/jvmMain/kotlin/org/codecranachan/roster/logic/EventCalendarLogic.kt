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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EventCalendarLogic(private val eventRepository: EventRepository) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun getEvent(eventId: Uuid): Event? {
        return eventRepository.getEvent(eventId)
    }

    fun updateEvent(eventId: Uuid, details: EventDetails) {
        eventRepository.updateEvent(eventId, details)
    }

    fun get(linkedGuildId: Uuid): EventCalendar {
        return EventCalendar(
            linkedGuildId,
            eventRepository.getEventsByGuild(linkedGuildId)
        )
    }

    fun addEvent(linkedGuildId: Uuid, event: Event) {
        eventRepository.addEvent(event.copy(guildId = linkedGuildId))
    }

    fun registerPlayer(eventId: Uuid, playerId: Uuid, tableId: Uuid?) {
        if (!eventRepository.isDungeonMasterForEvent(playerId, eventId)) {
            eventRepository.addRegistration(EventRegistration(uuid4(), eventId, playerId, tableId))
            logger.info("Registered $playerId as player with $eventId")
        }
    }

    fun updatePlayerRegistration(eventId: Uuid, playerId: Uuid, tableId: Uuid?) {
        if (eventRepository.isPlayerForEvent(playerId, eventId)) {
            eventRepository.updateRegistration(eventId, playerId, tableId)
        }
    }

    fun unregisterPlayer(eventId: Uuid, playerId: Uuid) {
        if (eventRepository.isPlayerForEvent(playerId, eventId)) {
            eventRepository.deleteRegistration(eventId, playerId)
        }
    }

    fun getTable(tableId: Uuid): Table? {
        return eventRepository.getHosting(tableId)
    }

    fun hostTable(hosting: TableHosting) {
        if (!eventRepository.isPlayerForEvent(hosting.dungeonMasterId, hosting.eventId)) {
            eventRepository.addHosting(hosting)
            logger.info("Registered ${hosting.dungeonMasterId} as dungeon master with ${hosting.eventId}")
        }
    }

    fun updateTable(tableId: Uuid, details: TableDetails) {
        eventRepository.updateHosting(tableId, details)
    }

    fun cancelTable(eventId: Uuid, dmId: Uuid) {
        if (eventRepository.isDungeonMasterForEvent(dmId, eventId)) {
            eventRepository.deleteHosting(eventId, dmId)
        }
    }
}