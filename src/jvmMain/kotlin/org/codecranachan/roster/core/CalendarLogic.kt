package org.codecranachan.roster.core

import com.benasher44.uuid.Uuid
import org.codecranachan.roster.core.events.CalendarEventCreated
import org.codecranachan.roster.core.events.CalendarEventUpdated
import org.codecranachan.roster.core.events.EventBus
import org.codecranachan.roster.core.events.RegistrationCreated
import org.codecranachan.roster.core.events.TableCreated
import org.codecranachan.roster.core.events.TableUpdated
import org.codecranachan.roster.query.CalendarQueryResult
import org.codecranachan.roster.query.EventQueryResult
import org.codecranachan.roster.query.TableQueryResult
import org.codecranachan.roster.repo.Repository

class UnknownGuildException(guildId: Uuid) : RosterLogicException("Guild does not exist: $guildId")
class UnknownPlayerException(playerId: Uuid) : RosterLogicException("Player does not exist: $playerId")
class UnknownEventException(eventId: Uuid) : RosterLogicException("Event does not exist: $eventId")
class PlayerAlreadyRegistered(eventId: Uuid, playerId: Uuid) :
    RosterLogicException("Player $playerId already registered for event $eventId")

class PlayerAlreadyHosting(eventId: Uuid, playerId: Uuid) :
    RosterLogicException("Player $playerId already hosting a table for event $eventId")

class EventCalendarLogic(
    repository: Repository,
    private val eventBus: EventBus
) {
    private val eventRepository = repository.eventRepository
    private val guildRepository = repository.guildRepository
    private val playerRepository = repository.playerRepository


    // -----
    // Queries
    // -----
    fun queryEvent(eventId: Uuid): EventQueryResult? {
        return eventRepository.queryEventData(eventId)
    }

    fun queryCalendar(linkedGuildId: Uuid): CalendarQueryResult? {
        guildRepository.getLinkedGuild(linkedGuildId) ?: return null
        return CalendarQueryResult(
            linkedGuildId,
            eventRepository.getEventsByGuild(linkedGuildId)
        )
    }

    // -----
    // Management
    // -----
    @kotlin.jvm.Throws(UnknownGuildException::class)
    fun addEvent(event: Event) {
        guildRepository.getLinkedGuild(event.guildId) ?: throw UnknownGuildException(event.guildId)
        eventRepository.addEvent(event)
        eventBus.publish(CalendarEventCreated(event))
    }

    fun updateEvent(eventId: Uuid, details: Event.Details) {
        eventRepository.updateEvent(eventId, details)
        publishEventChange(eventId)
    }

    fun addEvent(linkedGuildId: Uuid, event: Event) {
        val e = event.copy(guildId = linkedGuildId)
        eventRepository.addEvent(e)
        publishEventChange(e.id)
    }


    /**
     * Registers a player for a given event.
     */
    @kotlin.jvm.Throws(RosterLogicException::class)
    fun registerPlayer(eventId: Uuid, playerId: Uuid, details: Registration.Details = Registration.Details()) {
        eventRepository.getEvent(eventId) ?: throw UnknownEventException(eventId)
        playerRepository.getPlayer(playerId) ?: throw UnknownPlayerException(playerId)

        if (eventRepository.isDungeonMasterForEvent(playerId, eventId)) {
            throw PlayerAlreadyHosting(eventId, playerId)
        } else {
            val registration = eventRepository.getRegistration(eventId, playerId)
            if (registration == null) {
                val reg = Registration(
                    eventId = eventId,
                    playerId = playerId,
                    meta = Registration.Metadata(),
                    details = details
                )
                eventRepository.addRegistration(reg)
                eventBus.publish(RegistrationCreated(reg))
            } else {
                throw PlayerAlreadyRegistered(eventId, playerId)
            }
        }
    }

    fun updatePlayerRegistration(eventId: Uuid, playerId: Uuid, details: Registration.Details) {
        if (eventRepository.isPlayerForEvent(playerId, eventId)) {
            eventRepository.updateRegistration(eventId, playerId, details.dungeonMasterId)
            publishEventChange(eventId)
        }
    }

    fun unregisterPlayer(eventId: Uuid, playerId: Uuid) {
        if (eventRepository.isPlayerForEvent(playerId, eventId)) {
            eventRepository.deleteRegistration(eventId, playerId)
            publishEventChange(eventId)
        } else {
            throw RosterLogicException("Player is not registered")
        }
    }

    fun getTable(eventId: Uuid, dungeonMasterId: Uuid): TableQueryResult? {
        return eventRepository.getTable(eventId, dungeonMasterId)
    }

    fun hostTable(eventId: Uuid, dungeonMasterId: Uuid, details: Table.Details = Table.Details()) {
        eventRepository.getEvent(eventId) ?: throw UnknownEventException(eventId)
        playerRepository.getPlayer(dungeonMasterId) ?: throw UnknownPlayerException(dungeonMasterId)

        if (eventRepository.isPlayerForEvent(dungeonMasterId, eventId)) {
            throw PlayerAlreadyRegistered(eventId, dungeonMasterId)
        } else {
            val hostedTable = Table(eventId, dungeonMasterId)
            eventRepository.addTable(hostedTable)
            eventBus.publish(TableCreated(hostedTable))
        }
    }

    fun updateTable(eventId: Uuid, dungeonMasterId: Uuid, details: Table.Details) {
        eventRepository.updateHosting(eventId, dungeonMasterId, details)
        val query = eventRepository.getTable(eventId, dungeonMasterId)
        if (query != null) {
            eventBus.publish(TableUpdated(query.table))
        }
    }

    fun cancelTable(eventId: Uuid, dungeonMasterId: Uuid) {
        eventRepository.deleteHosting(eventId, dungeonMasterId)
    }

    private fun publishEventChange(eventId: Uuid) {
        eventRepository.queryEventData(eventId)?.let {
            eventBus.publish(CalendarEventUpdated(it.event))
        }
    }
}