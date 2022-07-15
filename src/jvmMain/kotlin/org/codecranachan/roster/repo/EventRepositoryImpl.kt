package org.codecranachan.roster.repo

import com.benasher44.uuid.Uuid
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalTime
import org.codecranachan.roster.Event
import org.codecranachan.roster.EventDetails
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
import org.codecranachan.roster.logic.EventRepository
import org.jooq.Condition
import org.jooq.Record
import java.time.OffsetDateTime

class EventRepositoryImpl(private val base: Repository) : EventRepository {

    override fun getEvent(eventId: Uuid): Event? {
        return fetchEventsWhere(EVENTS.ID.eq(eventId)).singleOrNull()
    }

    override fun getEventsByGuild(linkedGuildId: Uuid): List<Event> {
        return fetchEventsWhere(EVENTS.GUILD_ID.eq(linkedGuildId))
    }

    override fun addEvent(event: Event) {
        return base.withJooq {
            insertInto(EVENTS).set(event.asRecord()).execute()
        }
    }

    override fun updateEvent(eventId: Uuid, details: EventDetails) {
        return base.withJooq {
            update(EVENTS)
                .set(EVENTS.LOCATION, details.location)
                .set(EVENTS.EVENT_TIME, details.time?.toJavaLocalTime())
                .where(EVENTS.ID.eq(eventId))
                .execute()
        }
    }

    override fun addRegistration(registration: EventRegistration) {
        base.withJooq {
            insertInto(EVENTREGISTRATIONS).set(registration.asRecord()).execute()
        }
    }

    override fun updateRegistration(eventId: Uuid, playerId: Uuid, tableId: Uuid?) {
        return base.withJooq {
            update(EVENTREGISTRATIONS).set(EVENTREGISTRATIONS.TABLE_ID, tableId).where(
                EVENTREGISTRATIONS.EVENT_ID.eq(eventId),
                EVENTREGISTRATIONS.PLAYER_ID.eq(playerId),
            ).execute()
        }
    }

    override fun deleteRegistration(eventId: Uuid, playerId: Uuid) {
        return base.withJooq {
            deleteFrom(EVENTREGISTRATIONS).where(
                EVENTREGISTRATIONS.EVENT_ID.eq(eventId),
                EVENTREGISTRATIONS.PLAYER_ID.eq(playerId),
            ).execute()
        }
    }

    override fun getHosting(tableId: Uuid): Table? {
        return base.withJooq {
            select().from(HOSTEDTABLES).join(PLAYERS).on(HOSTEDTABLES.DUNGEON_MASTER_ID.eq(PLAYERS.ID))
                .where(HOSTEDTABLES.ID.eq(tableId)).fetchOne()?.map {
                    Table(
                        it[HOSTEDTABLES.ID],
                        playerFromRecord(it, PLAYERS),
                        tableDetailsFromRecord(it, HOSTEDTABLES)
                    )
                }
        }
    }

    override fun addHosting(hosting: TableHosting) {
        return base.withJooq {
            insertInto(HOSTEDTABLES).set(hosting.asRecord()).execute()
        }
    }

    override fun updateHosting(tableId: Uuid, details: TableDetails) {
        return base.withJooq {
            selectFrom(HOSTEDTABLES).where(HOSTEDTABLES.ID.eq(tableId)).fetchSingle().apply {
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

    override fun deleteHosting(eventId: Uuid, dmId: Uuid) {
        return base.withJooq {
            deleteFrom(HOSTEDTABLES).where(
                HOSTEDTABLES.EVENT_ID.eq(eventId),
                HOSTEDTABLES.DUNGEON_MASTER_ID.eq(dmId),
            ).execute()
        }
    }

    private fun fetchEventsWhere(condition: Condition): List<Event> {
        val dms = PLAYERS.`as`("dms")
        val pcs = PLAYERS.`as`("pcs")

        val fields =
            arrayOf(
                *EVENTS.fields(),
                *EVENTREGISTRATIONS.fields(),
                *HOSTEDTABLES.fields(),
                *pcs.fields(),
                *dms.fields()
            )

        return base.withJooq {
            val regSelect =
                select(*fields).from(EVENTS).leftJoin(EVENTREGISTRATIONS)
                    .on(EVENTREGISTRATIONS.EVENT_ID.eq(EVENTS.ID))
                    .leftJoin(HOSTEDTABLES).on(HOSTEDTABLES.ID.eq(EVENTREGISTRATIONS.TABLE_ID)).leftJoin(pcs)
                    .on(EVENTREGISTRATIONS.PLAYER_ID.eq(pcs.ID)).leftJoin(dms)
                    .on(HOSTEDTABLES.DUNGEON_MASTER_ID.eq(dms.ID))
                    .where(condition)
            val tblSelect = select(*fields).from(EVENTS).join(HOSTEDTABLES).on(HOSTEDTABLES.EVENT_ID.eq(EVENTS.ID))
                .leftJoin(EVENTREGISTRATIONS).on(HOSTEDTABLES.ID.eq(EVENTREGISTRATIONS.ID)).leftJoin(pcs)
                .on(EVENTREGISTRATIONS.PLAYER_ID.eq(pcs.ID)).leftJoin(dms)
                .on(HOSTEDTABLES.DUNGEON_MASTER_ID.eq(dms.ID))
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
                        } ?: listOf(),
                        EventDetails(
                            results.first()[EVENTS.EVENT_TIME]?.toKotlinLocalTime(),
                            results.first()[EVENTS.LOCATION]
                        )
                    )
                }
        }
    }

    override fun isPlayerForEvent(playerId: Uuid, eventId: Uuid): Boolean {
        return base.withJooq {
            selectCount().from(EVENTREGISTRATIONS).where(
                EVENTREGISTRATIONS.EVENT_ID.eq(eventId), EVENTREGISTRATIONS.PLAYER_ID.eq(playerId)
            ).fetchSingle().value1() > 0
        }
    }

    override fun isDungeonMasterForEvent(playerId: Uuid, eventId: Uuid): Boolean {
        return base.withJooq {
            selectCount().from(HOSTEDTABLES).where(
                HOSTEDTABLES.EVENT_ID.eq(eventId), HOSTEDTABLES.DUNGEON_MASTER_ID.eq(playerId)
            ).fetchSingle().value1() > 0
        }
    }

    private fun playerFromRecord(r: Record, t: Players): Player {
        return Player(
            r[t.ID],
            r[t.DISCORD_ID],
            r[t.DISCORD_NAME],
            r[t.DISCORD_AVATAR],
            PlayerDetails(
                r[t.PLAYER_NAME],
                Repository.decodeLanguages(r[t.LANGUAGES]),
                r[t.TIER_PREFERENCE]
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

    private fun Event.asRecord(): EventsRecord {
        return EventsRecord(id, date.toJavaLocalDate(), null, guildId, null, null, null)
    }

    private fun TableHosting.asRecord(): HostedtablesRecord {
        return HostedtablesRecord(
            id, eventId, dungeonMasterId, null, null, null, Tablelanguage.SwissGerman, 3, 7, 1, 4
        )
    }
}

