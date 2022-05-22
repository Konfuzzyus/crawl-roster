package org.codecranachan.roster.repo

import com.benasher44.uuid.Uuid
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.codecranachan.roster.Event
import org.codecranachan.roster.EventRegistration
import org.codecranachan.roster.Player
import org.codecranachan.roster.jooq.Tables.*
import org.codecranachan.roster.jooq.tables.records.EventregistrationsRecord
import org.codecranachan.roster.jooq.tables.records.EventsRecord
import org.codecranachan.roster.jooq.tables.records.PlayersRecord
import org.jooq.Condition

fun Repository.fetchEventsWhere(condition: Condition): List<Event> {
    return withJooq {
        select()
            .from(EVENTS)
            .leftJoin(EVENTREGISTRATIONS).on(EVENTREGISTRATIONS.EVENT_ID.eq(EVENTS.ID))
            .leftJoin(PLAYERS).on(EVENTREGISTRATIONS.PLAYER_ID.eq(PLAYERS.ID))
            .where(condition)
            .fetchGroups(EVENTS.fields(), PlayersRecord::class.java)
            .map { (event, players) ->
                Event(
                    event[EVENTS.ID],
                    event[EVENTS.GUILD_ID],
                    event[EVENTS.EVENT_DATE].toKotlinLocalDate(),
                    players.filter { it.id != null }.map { Player(it.id, it.playerName, it.discordName) },
                    listOf()
                )
            }
    }
}

fun Repository.fetchEventsByGuild(id: Uuid): List<Event> {
    return fetchEventsWhere(EVENTS.GUILD_ID.eq(id))
}

fun Repository.fetchEvent(id: Uuid): Event? {
    return fetchEventsWhere(EVENTS.ID.eq(id)).singleOrNull()
}

fun Repository.addEvent(event: Event) {
    return withJooq {
        insertInto(EVENTS).set(event.asRecord()).execute()
    }
}

fun Repository.addEventRegistration(reg: EventRegistration) {
    return withJooq {
        insertInto(EVENTREGISTRATIONS).set(reg.asRecord()).execute()
    }
}

fun Repository.removeEventRegistration(eventId: Uuid, playerId: Uuid) {
    return withJooq {
        deleteFrom(EVENTREGISTRATIONS)
            .where(
                EVENTREGISTRATIONS.EVENT_ID.eq(eventId),
                EVENTREGISTRATIONS.PLAYER_ID.eq(playerId),
            )
            .execute()
    }
}

fun EventRegistration.asRecord(): EventregistrationsRecord {
    return EventregistrationsRecord(id, eventId, playerId)
}

fun EventsRecord.asModel(): Event {
    return Event(id, guildId, eventDate.toKotlinLocalDate())
}

fun Event.asRecord(): EventsRecord {
    return EventsRecord(id, date.toJavaLocalDate(), guildId)
}