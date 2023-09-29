package org.codecranachan.roster.core

import com.benasher44.uuid.Uuid
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
import org.codecranachan.roster.query.CalendarQueryResult
import org.codecranachan.roster.query.EventQueryResult
import org.codecranachan.roster.query.RegistrationQueryResult
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

    fun queryTable(eventId: Uuid, dmId: Uuid): TableQueryResult? {
        return eventRepository.queryTableData(eventId, dmId)
    }

    // -----
    // Management
    // -----
    @kotlin.jvm.Throws(UnknownGuildException::class)
    fun addEvent(event: Event) {
        guildRepository.getLinkedGuild(event.guildId) ?: throw UnknownGuildException(event.guildId)
        eventRepository.addEvent(event)
        publishEventChange(event.id, ::CalendarEventCreated)
    }

    fun updateEvent(eventId: Uuid, details: Event.Details) {
        eventRepository.updateEvent(eventId, details)
        publishEventChange(eventId)
    }

    /**
     * Registers a player for a given event.
     */
    @kotlin.jvm.Throws(RosterLogicException::class)
    fun addPlayerRegistration(eventId: Uuid, playerId: Uuid, details: Registration.Details = Registration.Details()) {
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
                publishPlayerRegistration(eventId, playerId, ::RegistrationCreated)
            } else {
                throw PlayerAlreadyRegistered(eventId, playerId)
            }
        }
    }

    fun updatePlayerRegistration(eventId: Uuid, playerId: Uuid, details: Registration.Details) {
        if (eventRepository.isPlayerForEvent(playerId, eventId)) {
            eventRepository.updateRegistration(eventId, playerId, details.dungeonMasterId)
            publishPlayerRegistration(eventId, playerId)
        } else {
            throw RosterLogicException("Player is not registered")
        }
    }

    fun deletePlayerRegistration(eventId: Uuid, playerId: Uuid) {
        if (eventRepository.isPlayerForEvent(playerId, eventId)) {
            publishPlayerRegistration(eventId, playerId, ::RegistrationCanceled)
            eventRepository.deleteRegistration(eventId, playerId)
        } else {
            throw RosterLogicException("Player is not registered")
        }
    }

    fun addDmRegistration(eventId: Uuid, dungeonMasterId: Uuid, details: Table.Details = Table.Details()) {
        eventRepository.getEvent(eventId) ?: throw UnknownEventException(eventId)
        playerRepository.getPlayer(dungeonMasterId) ?: throw UnknownPlayerException(dungeonMasterId)

        if (eventRepository.isPlayerForEvent(dungeonMasterId, eventId)) {
            throw PlayerAlreadyRegistered(eventId, dungeonMasterId)
        } else {
            val hostedTable = Table(eventId, dungeonMasterId, details)
            eventRepository.addTable(hostedTable)
            publishTableChange(eventId, dungeonMasterId, ::TableCreated)
        }
    }

    fun updateDmRegistration(eventId: Uuid, dungeonMasterId: Uuid, details: Table.Details) {
        eventRepository.updateHosting(eventId, dungeonMasterId, details)
        publishTableChange(eventId, dungeonMasterId)
    }

    fun cancelDmRegistration(eventId: Uuid, dungeonMasterId: Uuid) {
        // needs to be published before deletion otherwise the data will already be gone
        publishTableChange(eventId, dungeonMasterId, ::TableCanceled)
        eventRepository.deleteHosting(eventId, dungeonMasterId)
    }

    private fun publishEventChange(
        eventId: Uuid,
        factory: (EventQueryResult) -> RosterEvent = ::CalendarEventUpdated
    ) {
        eventRepository.queryEventData(eventId)?.let {
            eventBus.publish(factory(it))
        }
    }

    private fun publishTableChange(
        eventId: Uuid,
        dungeonMasterId: Uuid,
        factory: (TableQueryResult) -> RosterEvent = ::TableUpdated
    ) {
        eventRepository.queryTableData(eventId, dungeonMasterId)?.let {
            eventBus.publish(factory(it))
        }
    }

    private fun publishPlayerRegistration(
        eventId: Uuid,
        playerId: Uuid,
        factory: (Registration) -> RosterEvent = ::RegistrationUpdated
    ) {
        eventRepository.getRegistration(eventId, playerId)?.let { reg ->
            eventBus.publish(factory(reg))
            publishEventChange(reg.eventId)
            reg.details.dungeonMasterId?.let { dmId -> publishTableChange(eventId, dmId) }
        }
    }
}