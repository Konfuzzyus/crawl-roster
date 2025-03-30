package org.codecranachan.roster.core

import com.benasher44.uuid.Uuid
import kotlinx.datetime.LocalDate
import org.codecranachan.roster.core.events.CalendarEventCanceled
import org.codecranachan.roster.core.events.CalendarEventCreated
import org.codecranachan.roster.core.events.CalendarEventUpdated
import org.codecranachan.roster.core.events.EventBus
import org.codecranachan.roster.core.events.RegistrationCanceled
import org.codecranachan.roster.core.events.RegistrationCreated
import org.codecranachan.roster.core.events.RegistrationUpdated
import org.codecranachan.roster.core.events.TableCanceled
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

class TableAlreadyFull(eventId: Uuid, dungeonMasterId: Uuid) :
    RosterLogicException("Table hosted by $dungeonMasterId for event $eventId is already full.")

class EventCalendarLogic(
    repository: Repository,
    private val eventBus: EventBus,
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

    fun queryCalendar(linkedGuildId: Uuid, after: LocalDate? = null, before: LocalDate? = null): CalendarQueryResult? {
        guildRepository.getLinkedGuild(linkedGuildId) ?: return null
        return CalendarQueryResult(
            linkedGuildId,
            eventRepository.getEventsByGuild(linkedGuildId, after, before)
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
        eventRepository.getEvent(event.id)?.let { eventBus.publish(CalendarEventCreated(it)) }
    }

    fun updateEvent(eventId: Uuid, details: Event.Details) {
        val previous = eventRepository.getEvent(eventId)
        eventRepository.updateEvent(eventId, details)
        val current = eventRepository.getEvent(eventId)
        eventBus.publish(CalendarEventUpdated(previous!!, current!!))
    }

    fun cancelEvent(eventId: Uuid) {
        val previous = eventRepository.getEvent(eventId)
        if (previous != null) {
            eventRepository.deleteEvent(eventId)
            eventBus.publish(CalendarEventCanceled(previous))
        }
    }

    fun getPlayerRegistration(eventId: Uuid, playerId: Uuid): Registration? {
        return eventRepository.getRegistration(eventId, playerId)
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
                val table = details.dungeonMasterId?.let { eventRepository.queryTableData(eventId, it) }
                if (table != null && table.isFull()) {
                    throw TableAlreadyFull(eventId, table.dm.id)
                }
                val reg = Registration(
                    eventId = eventId,
                    playerId = playerId,
                    meta = Registration.Metadata(),
                    details = details
                )
                eventRepository.addRegistration(reg)
                eventRepository.getRegistration(eventId, playerId)?.let { eventBus.publish(RegistrationCreated(it)) }
            } else {
                throw PlayerAlreadyRegistered(eventId, playerId)
            }
        }
    }

    fun updatePlayerRegistration(eventId: Uuid, playerId: Uuid, details: Registration.Details) {
        if (eventRepository.isPlayerForEvent(playerId, eventId)) {
            val table = details.dungeonMasterId?.let { eventRepository.queryTableData(eventId, it) }
            if (table != null && table.isFull()) {
                throw TableAlreadyFull(eventId, table.dm.id)
            }

            val previous = eventRepository.getRegistration(eventId, playerId)
            eventRepository.updateRegistration(eventId, playerId, details.dungeonMasterId)
            val current = eventRepository.getRegistration(eventId, playerId)
            eventBus.publish(RegistrationUpdated(previous!!, current!!))
        } else {
            throw RosterLogicException("Player is not registered")
        }
    }

    fun deletePlayerRegistration(eventId: Uuid, playerId: Uuid) {
        if (eventRepository.isPlayerForEvent(playerId, eventId)) {
            val previous = eventRepository.getRegistration(eventId, playerId)
            eventRepository.deleteRegistration(eventId, playerId)
            previous?.let { eventBus.publish(RegistrationCanceled(it)) }
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
            eventRepository.getTable(eventId, dungeonMasterId)?.let { eventBus.publish(TableCreated(it)) }
        }
    }

    fun updateDmRegistration(eventId: Uuid, dungeonMasterId: Uuid, details: Table.Details) {
        val previous = eventRepository.getTable(eventId, dungeonMasterId)
        eventRepository.updateHosting(eventId, dungeonMasterId, details)
        val current = eventRepository.getTable(eventId, dungeonMasterId)
        eventBus.publish(TableUpdated(previous!!, current!!))
    }

    fun cancelDmRegistration(eventId: Uuid, dungeonMasterId: Uuid) {
        // needs to be published before deletion otherwise the data will already be gone
        val previous = eventRepository.getTable(eventId, dungeonMasterId)
        eventRepository.deleteHosting(eventId, dungeonMasterId)
        eventBus.publish(TableCanceled(previous!!))
    }

}