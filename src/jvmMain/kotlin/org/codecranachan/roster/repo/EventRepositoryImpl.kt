package org.codecranachan.roster.repo

import com.benasher44.uuid.Uuid
import kotlinx.datetime.toJavaLocalTime
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Registration
import org.codecranachan.roster.core.Table
import org.codecranachan.roster.jooq.enums.Tablelanguage
import org.codecranachan.roster.jooq.tables.records.EventregistrationsRecord
import org.codecranachan.roster.jooq.tables.records.HostedtablesRecord
import org.codecranachan.roster.jooq.tables.records.PlayersRecord
import org.codecranachan.roster.jooq.tables.references.EVENTREGISTRATIONS
import org.codecranachan.roster.jooq.tables.references.EVENTS
import org.codecranachan.roster.jooq.tables.references.HOSTEDTABLES
import org.codecranachan.roster.jooq.tables.references.PLAYERS
import org.codecranachan.roster.query.EventQueryResult
import org.codecranachan.roster.query.TableQueryResult
import org.jooq.Condition

class EventRepository(private val base: Repository) {

    fun queryEventData(eventId: Uuid): EventQueryResult? {
        return fetchEventsWhere(EVENTS.ID.eq(eventId)).singleOrNull()
    }

    fun getEventsByGuild(linkedGuildId: Uuid): List<EventQueryResult> {
        return fetchEventsWhere(EVENTS.GUILD_ID.eq(linkedGuildId))
    }

    fun getEvent(eventId: Uuid): Event? {
        return base.withJooq {
            selectFrom(EVENTS)
                .where(EVENTS.ID.eq(eventId))
                .fetchOne()
                ?.asModel()
        }
    }

    fun addEvent(event: Event) {
        return base.withJooq {
            insertInto(EVENTS).set(event.asRecord()).execute()
        }
    }

    fun updateEvent(eventId: Uuid, details: Event.Details) {
        return base.withJooq {
            update(EVENTS)
                .set(EVENTS.LOCATION, details.location)
                .set(EVENTS.EVENT_TIME, details.time?.toJavaLocalTime())
                .where(EVENTS.ID.eq(eventId))
                .execute()
        }
    }

    fun getRegistration(eventId: Uuid, playerId: Uuid): Registration? {
        return base.withJooq {
            selectFrom(EVENTREGISTRATIONS).where(
                EVENTREGISTRATIONS.EVENT_ID.eq(eventId),
                EVENTREGISTRATIONS.PLAYER_ID.eq(playerId)
            ).fetchOne()?.asModel()
        }
    }

    fun addRegistration(registration: Registration) {
        base.withJooq {
            insertInto(EVENTREGISTRATIONS).set(registration.asRecord()).execute()
        }
    }

    fun updateRegistration(eventId: Uuid, playerId: Uuid, dungeonMasterId: Uuid?) {
        return base.withJooq {
            update(EVENTREGISTRATIONS).set(EVENTREGISTRATIONS.DUNGEON_MASTER_ID, dungeonMasterId).where(
                EVENTREGISTRATIONS.EVENT_ID.eq(eventId),
                EVENTREGISTRATIONS.PLAYER_ID.eq(playerId),
            ).execute()
        }
    }

    fun deleteRegistration(eventId: Uuid, playerId: Uuid) {
        return base.withJooq {
            deleteFrom(EVENTREGISTRATIONS).where(
                EVENTREGISTRATIONS.EVENT_ID.eq(eventId),
                EVENTREGISTRATIONS.PLAYER_ID.eq(playerId),
            ).execute()
        }
    }

    fun getTable(eventId: Uuid, dungeonMasterId: Uuid): TableQueryResult? {
        val regs = base.withJooq {
            select().from(EVENTREGISTRATIONS)
                .join(PLAYERS).on(EVENTREGISTRATIONS.PLAYER_ID.eq(PLAYERS.ID))
                .where(
                    EVENTREGISTRATIONS.EVENT_ID.eq(eventId),
                    EVENTREGISTRATIONS.DUNGEON_MASTER_ID.eq(dungeonMasterId)
                )
                .fetch()
        }
        val tbl = base.withJooq {
            select().from(HOSTEDTABLES)
                .join(PLAYERS).on(HOSTEDTABLES.DUNGEON_MASTER_ID.eq(PLAYERS.ID))
                .where(
                    HOSTEDTABLES.EVENT_ID.eq(eventId),
                    HOSTEDTABLES.DUNGEON_MASTER_ID.eq(dungeonMasterId)
                )
                .fetchOne()
        }

        return tbl?.let { t ->
            TableQueryResult(
                t.into(HostedtablesRecord::class.java).asModel(),
                t.into(PlayersRecord::class.java).asModel(),
                regs.into(PlayersRecord::class.java).map { it.asModel() }
            )
        }
    }

    fun addTable(table: Table) {
        return base.withJooq {
            insertInto(HOSTEDTABLES).set(table.asRecord()).execute()
        }
    }

    fun updateHosting(eventId: Uuid, dungeonMasterId: Uuid, details: Table.Details) {
        return base.withJooq {
            selectFrom(HOSTEDTABLES).where(
                HOSTEDTABLES.EVENT_ID.eq(eventId),
                HOSTEDTABLES.DUNGEON_MASTER_ID.eq(dungeonMasterId)
            ).fetchSingle().apply {
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

    fun deleteHosting(eventId: Uuid, dungeonMasterId: Uuid): Int {
        return base.withJooq {
            update(EVENTREGISTRATIONS)
                .setNull(EVENTREGISTRATIONS.DUNGEON_MASTER_ID)
                .where(
                    EVENTREGISTRATIONS.EVENT_ID.eq(eventId),
                    EVENTREGISTRATIONS.DUNGEON_MASTER_ID.eq(dungeonMasterId)
                )
                .execute()
            deleteFrom(HOSTEDTABLES).where(
                HOSTEDTABLES.EVENT_ID.eq(eventId),
                HOSTEDTABLES.DUNGEON_MASTER_ID.eq(dungeonMasterId)
            ).execute()
        }
    }

    private fun fetchEventsWhere(condition: Condition): List<EventQueryResult> {
        val evts = base.withJooq {
            selectFrom(EVENTS)
                .where(condition)
                .fetch()
        }
        val regs = base.withJooq {
            select().from(EVENTREGISTRATIONS)
                .join(PLAYERS).on(EVENTREGISTRATIONS.PLAYER_ID.eq(PLAYERS.ID))
                .where(condition)
                .fetch()
                .groupBy { it[EVENTS.ID] }
        }
        val tbls = base.withJooq {
            select().from(HOSTEDTABLES)
                .join(PLAYERS).on(HOSTEDTABLES.DUNGEON_MASTER_ID.eq(PLAYERS.ID))
                .where(condition)
                .fetch()
                .groupBy { it[EVENTS.ID] }
        }
        return evts.map { evt ->
            val r = regs.getOrElse(evt.id, ::emptyList)
            val e = regs.getOrElse(evt.id, ::emptyList)
            EventQueryResult(
                evt.asModel(),
                regs.getOrElse(evt.id, ::emptyList).map { it.into(EventregistrationsRecord::class.java).asModel() },
                tbls.getOrElse(evt.id, ::emptyList).map { it.into(HostedtablesRecord::class.java).asModel() },
                (r + e).map { it.into(PlayersRecord::class.java).asModel() }.distinctBy { it.id }
            )
        }
    }

    fun isDungeonMasterForEvent(playerId: Uuid, eventId: Uuid): Boolean {
        return base.withJooq {
            selectFrom(HOSTEDTABLES)
                .where(
                    HOSTEDTABLES.DUNGEON_MASTER_ID.eq(playerId),
                    HOSTEDTABLES.EVENT_ID.eq(eventId)
                )
                .fetchOne()
        } != null
    }

    fun isPlayerForEvent(playerId: Uuid, eventId: Uuid): Boolean {
        return base.withJooq {
            selectFrom(EVENTREGISTRATIONS)
                .where(
                    EVENTREGISTRATIONS.PLAYER_ID.eq(playerId),
                    EVENTREGISTRATIONS.EVENT_ID.eq(eventId)
                )
                .fetchOne()
        } != null
    }
}





