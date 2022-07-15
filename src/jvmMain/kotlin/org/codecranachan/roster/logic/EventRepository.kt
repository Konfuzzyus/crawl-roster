package org.codecranachan.roster.logic

import com.benasher44.uuid.Uuid
import org.codecranachan.roster.Event
import org.codecranachan.roster.EventDetails
import org.codecranachan.roster.EventRegistration
import org.codecranachan.roster.Table
import org.codecranachan.roster.TableDetails
import org.codecranachan.roster.TableHosting

interface EventRepository {
    fun getEvent(eventId: Uuid): Event?
    fun getEventsByGuild(linkedGuildId: Uuid): List<Event>
    fun addEvent(event: Event)
    fun updateEvent(eventId: Uuid, details: EventDetails)

    fun isPlayerForEvent(playerId: Uuid, eventId: Uuid): Boolean
    fun isDungeonMasterForEvent(playerId: Uuid, eventId: Uuid): Boolean

    // todo: clean up interface signature mess
    fun addRegistration(registration: EventRegistration)
    fun updateRegistration(eventId: Uuid, playerId: Uuid, tableId: Uuid?)
    fun deleteRegistration(eventId: Uuid, playerId: Uuid)

    // todo: clean up interface signature mess
    fun getHosting(tableId: Uuid): Table?
    fun addHosting(hosting: TableHosting)
    fun updateHosting(tableId: Uuid, details: TableDetails)
    fun deleteHosting(eventId: Uuid, dmId: Uuid)

}