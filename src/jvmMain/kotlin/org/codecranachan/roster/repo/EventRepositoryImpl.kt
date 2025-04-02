package org.codecranachan.roster.repo

import com.benasher44.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Registration
import org.codecranachan.roster.core.Table
import org.codecranachan.roster.jooq.enums.Tableaudience
import org.codecranachan.roster.jooq.enums.Tablelanguage
import org.codecranachan.roster.jooq.tables.records.EventregistrationsRecord
import org.codecranachan.roster.jooq.tables.records.HostedtablesRecord
import org.codecranachan.roster.jooq.tables.records.PlayersRecord
import org.codecranachan.roster.jooq.tables.references.EVENTREGISTRATIONS
import org.codecranachan.roster.jooq.tables.references.EVENTS
import org.codecranachan.roster.jooq.tables.references.HOSTEDTABLES
import org.codecranachan.roster.jooq.tables.references.PLAYERS
import org.codecranachan.roster.query.EventQueryResult
import org.codecranachan.roster.query.EventStatisticsQueryResult
import org.codecranachan.roster.query.TableQueryResult
import org.jooq.Condition
import org.jooq.TableField
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl.createField

class EventRepository(private val base: Repository) {

    fun queryEventData(eventId: Uuid): EventQueryResult? {
        return fetchEventsWhere(EVENTS.ID.eq(eventId)).singleOrNull()
    }

    fun getEventsByGuild(linkedGuildId: Uuid, after: LocalDate?, before: LocalDate?): List<EventQueryResult> {
        val conditions = listOfNotNull(
            EVENTS.GUILD_ID.eq(linkedGuildId),
            after?.let { EVENTS.EVENT_DATE.ge(it.toJavaLocalDate()) },
            before?.let { EVENTS.EVENT_DATE.le(it.toJavaLocalDate()) },
        )

        return fetchEventsWhere(
            DSL.and(conditions)
        )
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

    fun deleteEvent(eventId: Uuid) {
        base.withJooq {
            deleteFrom(EVENTREGISTRATIONS)
                .where(EVENTREGISTRATIONS.EVENT_ID.eq(eventId))
                .execute()
            deleteFrom(HOSTEDTABLES)
                .where(HOSTEDTABLES.EVENT_ID.eq(eventId))
                .execute()
            deleteFrom(EVENTS)
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

    fun queryTableData(eventId: Uuid, dungeonMasterId: Uuid): TableQueryResult? {
        val event = getEvent(eventId) ?: return null
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
                event,
                t.into(HostedtablesRecord::class.java).asModel(),
                t.into(PlayersRecord::class.java).asModel(),
                regs.sortedBy { it[EVENTREGISTRATIONS.REGISTRATION_TIME] }
                    .map { it.into(PlayersRecord::class.java).asModel() }
            )
        }
    }

    fun getTable(eventId: Uuid, dungeonMasterId: Uuid): Table? {
        return base.withJooq {
            selectFrom(HOSTEDTABLES).where(
                HOSTEDTABLES.EVENT_ID.eq(eventId),
                HOSTEDTABLES.DUNGEON_MASTER_ID.eq(dungeonMasterId)
            ).fetchOne()?.asModel()
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
                audience = Tableaudience.valueOf(details.audience.name)
                gameSystem = details.gameSystem?.ifBlank { null }
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
                .orderBy(EVENTS.EVENT_DATE)
                .fetch()
        }
        val eventIds = evts.map { it[EVENTS.ID]!! }

        val regs = base.withJooq {
            select().from(EVENTREGISTRATIONS)
                .join(PLAYERS).on(EVENTREGISTRATIONS.PLAYER_ID.eq(PLAYERS.ID))
                .where(EVENTREGISTRATIONS.EVENT_ID.`in`(eventIds))
                .fetch()
                .groupBy { it[HOSTEDTABLES.EVENT_ID] }
        }
        val tbls = base.withJooq {
            select().from(HOSTEDTABLES)
                .join(PLAYERS).on(HOSTEDTABLES.DUNGEON_MASTER_ID.eq(PLAYERS.ID))
                .where(HOSTEDTABLES.EVENT_ID.`in`(eventIds))
                .fetch()
                .groupBy { it[HOSTEDTABLES.EVENT_ID] }
        }
        return evts.map { evt ->
            val pcs = regs.getOrElse(evt.id, ::emptyList)
            val dms = tbls.getOrElse(evt.id, ::emptyList)
            EventQueryResult(
                evt.asModel(),
                regs.getOrElse(evt.id, ::emptyList).map { it.into(EventregistrationsRecord::class.java).asModel() },
                tbls.getOrElse(evt.id, ::emptyList).map { it.into(HostedtablesRecord::class.java).asModel() },
                (pcs + dms).map { it.into(PlayersRecord::class.java).asModel() }.distinctBy { it.id }
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

    fun queryEventStats(linkedGuildId: Uuid, after: LocalDate?, before: LocalDate?): EventStatisticsQueryResult {
        val eventStats = EventStatisticsQueryResult.EventStatistics(
            base.withJooq {
                fetchCount(
                    EVENTS,
                    EVENTS.GUILD_ID.eq(linkedGuildId),
                    after?.let { EVENTS.EVENT_DATE.ge(it.toJavaLocalDate()) },
                    before?.let { EVENTS.EVENT_DATE.le(it.toJavaLocalDate()) },
                )
            },
            base.withJooq {
                fetchCount(
                    HOSTEDTABLES.join(EVENTS).onKey(),
                    EVENTS.GUILD_ID.eq(linkedGuildId),
                    after?.let { EVENTS.EVENT_DATE.ge(it.toJavaLocalDate()) },
                    before?.let { EVENTS.EVENT_DATE.le(it.toJavaLocalDate()) },
                )
            },
            base.withJooq {
                fetchCount(
                    EVENTREGISTRATIONS.join(EVENTS).onKey(),
                    EVENTS.GUILD_ID.eq(linkedGuildId),
                    after?.let { EVENTS.EVENT_DATE.ge(it.toJavaLocalDate()) },
                    before?.let { EVENTS.EVENT_DATE.le(it.toJavaLocalDate()) },
                    EVENTREGISTRATIONS.DUNGEON_MASTER_ID.isNotNull
                )
            },
            base.withJooq {
                fetchCount(
                    selectDistinct(EVENTREGISTRATIONS.PLAYER_ID)
                        .from(EVENTREGISTRATIONS.join(EVENTS).onKey())
                        .where(
                            EVENTS.GUILD_ID.eq(linkedGuildId),
                            after?.let { EVENTS.EVENT_DATE.ge(it.toJavaLocalDate()) },
                            before?.let { EVENTS.EVENT_DATE.le(it.toJavaLocalDate()) })
                )
            }
        )

        val dmStats = base.withJooq {
            select(
                HOSTEDTABLES.DUNGEON_MASTER_ID,
                DSL.countDistinct(EVENTS.ID),
                DSL.countDistinct(EVENTREGISTRATIONS.PLAYER_ID),
                DSL.count(EVENTREGISTRATIONS.PLAYER_ID)
            ).from(
                HOSTEDTABLES
                    .join(EVENTS).onKey()
                    .join(EVENTREGISTRATIONS).on(
                        HOSTEDTABLES.DUNGEON_MASTER_ID.eq(EVENTREGISTRATIONS.DUNGEON_MASTER_ID),
                        EVENTS.ID.eq(EVENTREGISTRATIONS.EVENT_ID)
                    )
            ).where(
                EVENTS.GUILD_ID.eq(linkedGuildId),
                after?.let { EVENTS.EVENT_DATE.ge(it.toJavaLocalDate()) },
                before?.let { EVENTS.EVENT_DATE.le(it.toJavaLocalDate()) },
            ).groupBy(HOSTEDTABLES.DUNGEON_MASTER_ID)
        }

        val playerMap = base.withJooq { selectFrom(PLAYERS).where(PLAYERS.ID.`in`(dmStats.map { it.value1() })) }
            .associateBy { it[PLAYERS.ID] }


        return EventStatisticsQueryResult(
            eventStats,
            dmStats.map {
                EventStatisticsQueryResult.DungeonMasterStatistics(
                    playerMap[it.value1()]!!.asModel(),
                    it.value2(),
                    it.value4(),
                    it.value3()
                )
            }.sortedByDescending { it.tablesHosted }
        )
    }
}





