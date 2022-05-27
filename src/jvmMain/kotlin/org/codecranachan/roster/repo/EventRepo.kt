package org.codecranachan.roster.repo

import com.benasher44.uuid.Uuid
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.html.Entities
import org.codecranachan.roster.*
import org.codecranachan.roster.jooq.Tables.*
import org.codecranachan.roster.jooq.tables.records.EventregistrationsRecord
import org.codecranachan.roster.jooq.tables.records.EventsRecord
import org.codecranachan.roster.jooq.tables.records.HostedtablesRecord
import org.jooq.Condition

fun Repository.fetchEventsWhere(condition: Condition): List<Event> {
    val dms = PLAYERS.`as`("dms")
    val pcs = PLAYERS.`as`("pcs")

    return withJooq {
        select()
            .from(EVENTS)
            .leftJoin(EVENTREGISTRATIONS).on(EVENTREGISTRATIONS.EVENT_ID.eq(EVENTS.ID))
            .leftJoin(HOSTEDTABLES).on(HOSTEDTABLES.EVENT_ID.eq(EVENTS.ID))
            .leftJoin(pcs).on(EVENTREGISTRATIONS.PLAYER_ID.eq(pcs.ID))
            .leftJoin(dms).on(HOSTEDTABLES.DUNGEON_MASTER_ID.eq(dms.ID))
            .where(condition)
            .fetchGroups(EVENTS.ID)
            .map { (id, rows) ->
                Event(
                    id,
                    rows.first()[EVENTS.GUILD_ID],
                    rows.first()[EVENTS.EVENT_DATE].toKotlinLocalDate(),
                    rows.filter { it[pcs.ID] != null }
                        .map { Player(it[pcs.ID], it[pcs.PLAYER_NAME], it[pcs.DISCORD_NAME]) }.distinct(),
                    rows.filter { it[HOSTEDTABLES.ID] != null }.map {
                        Table(
                            it[HOSTEDTABLES.ID],
                            Player(
                                it[dms.ID],
                                it[dms.PLAYER_NAME],
                                it[dms.DISCORD_NAME]
                            )
                        )
                    }.distinct()
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

fun Repository.isRegisteredForEvent(playerId: Uuid, eventId: Uuid): Boolean {
    return withJooq {
        selectCount()
            .from(EVENTREGISTRATIONS)
            .where(
                EVENTREGISTRATIONS.EVENT_ID.eq(eventId),
                EVENTREGISTRATIONS.PLAYER_ID.eq(playerId)
            )
            .fetchSingle()
            .value1() > 0
    }
}

fun Repository.isHostingForEvent(playerId: Uuid, eventId: Uuid): Boolean {
    return withJooq {
        selectCount()
            .from(HOSTEDTABLES)
            .where(
                HOSTEDTABLES.EVENT_ID.eq(eventId),
                HOSTEDTABLES.DUNGEON_MASTER_ID.eq(playerId)
            )
            .fetchSingle()
            .value1() > 0
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

fun Repository.addHostedTable(tab: TableHosting) {
    return withJooq {
        insertInto(HOSTEDTABLES).set(tab.asRecord()).execute()
    }
}

fun Repository.removeHostedTable(eventId: Uuid, dmId: Uuid) {
    return withJooq {
        deleteFrom(HOSTEDTABLES)
            .where(
                HOSTEDTABLES.EVENT_ID.eq(eventId),
                HOSTEDTABLES.DUNGEON_MASTER_ID.eq(dmId),
            )
            .execute()
    }
}

fun EventRegistration.asRecord(): EventregistrationsRecord {
    return EventregistrationsRecord(id, eventId, playerId, null, null)
}

fun EventsRecord.asModel(): Event {
    return Event(id, guildId, eventDate.toKotlinLocalDate())
}

fun Event.asRecord(): EventsRecord {
    return EventsRecord(id, date.toJavaLocalDate(), guildId)
}

fun TableHosting.asRecord(): HostedtablesRecord {
    return HostedtablesRecord(
        id, eventId, dungeonMasterId
    )
}