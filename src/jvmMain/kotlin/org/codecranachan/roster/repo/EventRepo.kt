package org.codecranachan.roster.repo

import com.benasher44.uuid.Uuid
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.codecranachan.roster.Event
import org.codecranachan.roster.EventRegistration
import org.codecranachan.roster.PlaySession
import org.codecranachan.roster.Player
import org.codecranachan.roster.PlayerDetails
import org.codecranachan.roster.Table
import org.codecranachan.roster.TableDetails
import org.codecranachan.roster.TableHosting
import org.codecranachan.roster.TableLanguage
import org.codecranachan.roster.jooq.Tables.EVENTREGISTRATIONS
import org.codecranachan.roster.jooq.Tables.EVENTS
import org.codecranachan.roster.jooq.Tables.HOSTEDTABLES
import org.codecranachan.roster.jooq.Tables.PLAYERS
import org.codecranachan.roster.jooq.enums.Tablelanguage
import org.codecranachan.roster.jooq.tables.Hostedtables
import org.codecranachan.roster.jooq.tables.Players
import org.codecranachan.roster.jooq.tables.records.EventregistrationsRecord
import org.codecranachan.roster.jooq.tables.records.EventsRecord
import org.codecranachan.roster.jooq.tables.records.HostedtablesRecord
import org.jooq.Condition
import org.jooq.Record
import org.jooq.Result
import java.time.OffsetDateTime


private fun Repository.fetchTables(condition: Condition): Map<Uuid, Result<Record>> {
    return withJooq {
        select().from(EVENTS).join(HOSTEDTABLES).on(HOSTEDTABLES.EVENT_ID.eq(EVENTS.ID)).join(PLAYERS)
            .on(HOSTEDTABLES.DUNGEON_MASTER_ID.eq(EVENTS.ID)).where(condition).fetchGroups(EVENTS.ID)
    }
}

private fun Repository.fetchRegistrations(condition: Condition): Map<Uuid, Result<Record>> {
    return withJooq {
        select().from(EVENTS).leftJoin(EVENTREGISTRATIONS).on(EVENTREGISTRATIONS.EVENT_ID.eq(EVENTS.ID))
            .leftJoin(PLAYERS).on(EVENTREGISTRATIONS.PLAYER_ID.eq(PLAYERS.ID)).where(condition).fetchGroups(EVENTS.ID)
    }
}

fun Repository.fetchEventsWhere(condition: Condition): List<Event> {
    val dms = PLAYERS.`as`("dms")
    val pcs = PLAYERS.`as`("pcs")

    val fields =
        arrayOf(*EVENTS.fields(), *EVENTREGISTRATIONS.fields(), *HOSTEDTABLES.fields(), *pcs.fields(), *dms.fields())

    return withJooq {
        val regSelect =
            select(*fields).from(EVENTS).leftJoin(EVENTREGISTRATIONS).on(EVENTREGISTRATIONS.EVENT_ID.eq(EVENTS.ID))
                .leftJoin(HOSTEDTABLES).on(HOSTEDTABLES.ID.eq(EVENTREGISTRATIONS.TABLE_ID)).leftJoin(pcs)
                .on(EVENTREGISTRATIONS.PLAYER_ID.eq(pcs.ID)).leftJoin(dms).on(HOSTEDTABLES.DUNGEON_MASTER_ID.eq(dms.ID))
                .where(condition)
        val tblSelect = select(*fields).from(EVENTS).join(HOSTEDTABLES).on(HOSTEDTABLES.EVENT_ID.eq(EVENTS.ID))
            .leftJoin(EVENTREGISTRATIONS).on(HOSTEDTABLES.ID.eq(EVENTREGISTRATIONS.ID)).leftJoin(pcs)
            .on(EVENTREGISTRATIONS.PLAYER_ID.eq(pcs.ID)).leftJoin(dms).on(HOSTEDTABLES.DUNGEON_MASTER_ID.eq(dms.ID))
            .where(
                EVENTREGISTRATIONS.ID.isNull, condition
            )

        regSelect.union(tblSelect).orderBy(EVENTS.EVENT_DATE.asc(), EVENTREGISTRATIONS.REGISTRATION_TIME.asc())
            .fetchGroups(EVENTS.ID).map { (id, results) ->
                val byTables = results.groupBy {
                    if (it[HOSTEDTABLES.ID] == null) {
                        null
                    } else {
                        Table(
                            it[HOSTEDTABLES.ID],
                            playerFromRecord(it, dms),
                            tableDetailsFromRecord(it, HOSTEDTABLES)
                        )
                    }
                }
                Event(id,
                    results.first()[EVENTS.GUILD_ID],
                    results.first()[EVENTS.EVENT_DATE].toKotlinLocalDate(),
                    byTables.filterKeys { it != null }.map { e ->
                        val rows = e.value
                        PlaySession(
                            e.key!!,
                            rows.filter { it[pcs.ID] != null }.map { playerFromRecord(it, pcs) }.distinct()
                        )
                    },
                    byTables[null]?.let { rows ->
                        rows.filter { it[pcs.ID] != null }.map { playerFromRecord(it, pcs) }.distinct()
                    } ?: listOf()
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
        selectCount().from(EVENTREGISTRATIONS).where(
            EVENTREGISTRATIONS.EVENT_ID.eq(eventId), EVENTREGISTRATIONS.PLAYER_ID.eq(playerId)
        ).fetchSingle().value1() > 0
    }
}

fun Repository.isHostingForEvent(playerId: Uuid, eventId: Uuid): Boolean {
    return withJooq {
        selectCount().from(HOSTEDTABLES).where(
            HOSTEDTABLES.EVENT_ID.eq(eventId), HOSTEDTABLES.DUNGEON_MASTER_ID.eq(playerId)
        ).fetchSingle().value1() > 0
    }
}

fun Repository.addEventRegistration(reg: EventRegistration) {
    return withJooq {
        insertInto(EVENTREGISTRATIONS).set(reg.asRecord()).execute()
    }
}

fun Repository.removeEventRegistration(eventId: Uuid, playerId: Uuid) {
    return withJooq {
        deleteFrom(EVENTREGISTRATIONS).where(
            EVENTREGISTRATIONS.EVENT_ID.eq(eventId),
            EVENTREGISTRATIONS.PLAYER_ID.eq(playerId),
        ).execute()
    }
}

fun Repository.updateEventRegistration(eventId: Uuid, playerId: Uuid, tableId: Uuid?) {
    return withJooq {
        update(EVENTREGISTRATIONS).set(EVENTREGISTRATIONS.TABLE_ID, tableId).where(
            EVENTREGISTRATIONS.EVENT_ID.eq(eventId),
            EVENTREGISTRATIONS.PLAYER_ID.eq(playerId),
        ).execute()
    }
}

fun Repository.addHostedTable(tab: TableHosting) {
    return withJooq {
        insertInto(HOSTEDTABLES).set(tab.asRecord()).execute()
    }
}

fun Repository.removeHostedTable(eventId: Uuid, dmId: Uuid) {
    return withJooq {
        deleteFrom(HOSTEDTABLES).where(
            HOSTEDTABLES.EVENT_ID.eq(eventId),
            HOSTEDTABLES.DUNGEON_MASTER_ID.eq(dmId),
        ).execute()
    }
}

fun Repository.fetchTable(id: Uuid): Table? {
    return withJooq {
        select().from(HOSTEDTABLES).join(PLAYERS).on(HOSTEDTABLES.DUNGEON_MASTER_ID.eq(PLAYERS.ID))
            .where(HOSTEDTABLES.ID.eq(id)).fetchOne()?.map {
                Table(
                    it[HOSTEDTABLES.ID],
                    playerFromRecord(it, PLAYERS),
                    tableDetailsFromRecord(it, HOSTEDTABLES)
                )
            }
    }
}

fun Repository.updateHostedTable(id: Uuid, details: TableDetails) {
    return withJooq {
        selectFrom(HOSTEDTABLES).where(HOSTEDTABLES.ID.eq(id)).fetchSingle().apply {
            adventureTitle = details.adventureTitle?.ifBlank { null }
            adventureDescription = details.adventureDescription?.ifBlank { null }
            moduleDesignation = details.moduleDesignation?.ifBlank { null }
            tableLanguage = Tablelanguage.valueOf(details.language.name)
            minPlayers = details.playerRange.first
            maxPlayers = details.playerRange.last
            minCharacterLevel = details.levelRange.first
            maxCharacterLevel = details.levelRange.last
        }.store()
    }
}

private fun playerFromRecord(r: Record, t: Players): Player {
    return Player(
        r[t.ID],
        r[t.DISCORD_NAME],
        r[t.DISCORD_AVATAR],
        PlayerDetails(
            r[t.PLAYER_NAME],
            Repository.decodeLanguages(r[t.LANGUAGES])
        )
    )
}

private fun tableDetailsFromRecord(r: Record, t: Hostedtables): TableDetails {
    return TableDetails(
        r[t.ADVENTURE_TITLE],
        r[t.ADVENTURE_DESCRIPTION],
        r[t.MODULE_DESIGNATION],
        TableLanguage.valueOf(r[t.TABLE_LANGUAGE].name),
        r[t.MIN_PLAYERS]..r[t.MAX_PLAYERS],
        r[t.MIN_CHARACTER_LEVEL]..r[t.MAX_CHARACTER_LEVEL]
    )
}

private fun EventRegistration.asRecord(): EventregistrationsRecord {
    return EventregistrationsRecord(id, eventId, playerId, null, tableId, OffsetDateTime.now())
}

private fun EventsRecord.asModel(): Event {
    return Event(id, guildId, eventDate.toKotlinLocalDate())
}

private fun Event.asRecord(): EventsRecord {
    return EventsRecord(id, date.toJavaLocalDate(), null, guildId, null, null, null)
}

private fun TableHosting.asRecord(): HostedtablesRecord {
    return HostedtablesRecord(
        id, eventId, dungeonMasterId, null, null, null, Tablelanguage.SwissGerman, 3, 7, 1, 4
    )
}