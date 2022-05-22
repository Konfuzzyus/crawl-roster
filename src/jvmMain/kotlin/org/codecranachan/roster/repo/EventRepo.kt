package org.codecranachan.roster.repo

import com.benasher44.uuid.Uuid
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.codecranachan.roster.Event
import org.codecranachan.roster.EventRegistration
import org.codecranachan.roster.jooq.Tables.EVENTREGISTRATIONS
import org.codecranachan.roster.jooq.Tables.EVENTS
import org.codecranachan.roster.jooq.tables.records.EventregistrationsRecord
import org.codecranachan.roster.jooq.tables.records.EventsRecord

fun Repository.fetchEventsByGuild(id: Uuid): List<Event> {
    return withJooq {
        selectFrom(EVENTS)
            .where(EVENTS.GUILD_ID.eq(id))
            .orderBy(EVENTS.EVENT_DATE.asc())
            .fetch()
            .toList().map { it.asModel() }
    }
}

fun Repository.fetchEvent(id: Uuid): Event? {
    return withJooq {
        selectFrom(EVENTS).where(EVENTS.ID.eq(id)).fetchOne()?.asModel()
    }
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

fun Repository.removeEventRegistration(id: Uuid) {
    return withJooq {
        deleteFrom(EVENTREGISTRATIONS).where(EVENTREGISTRATIONS.ID.eq(id)).execute()
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