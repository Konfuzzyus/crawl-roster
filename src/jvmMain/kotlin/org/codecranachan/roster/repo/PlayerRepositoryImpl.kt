package org.codecranachan.roster.repo

import com.benasher44.uuid.Uuid
import org.codecranachan.roster.LinkedGuild
import org.codecranachan.roster.core.GuildMembership
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.jooq.tables.references.GUILDROLES
import org.codecranachan.roster.jooq.tables.references.GUILDS
import org.codecranachan.roster.jooq.tables.references.PLAYERS

class PlayerRepository(private val base: Repository) {
    fun getPlayer(playerId: Uuid): Player? {
        return base.withJooq {
            selectFrom(PLAYERS).where(PLAYERS.ID.eq(playerId)).fetchOne()?.asModel()
        }
    }

    fun getPlayerByDiscordId(discordId: String): Player? {
        return base.withJooq {
            selectFrom(PLAYERS).where(PLAYERS.DISCORD_ID.eq(discordId)).fetchOne()?.asModel()
        }
    }

    fun addPlayer(player: Player) {
        return base.withJooq {
            insertInto(PLAYERS).set(player.asRecord()).execute()
        }
    }

    fun updatePlayer(playerId: Uuid, details: Player.Details) {
        return base.withJooq {
            update(PLAYERS)
                .set(
                    mapOf(
                        PLAYERS.PLAYER_NAME to details.name,
                        PLAYERS.LANGUAGES to Repository.encodeLanguages(details.languages),
                        PLAYERS.TIER_PREFERENCE to details.playTier
                    )
                )
                .where(PLAYERS.ID.eq(playerId))
                .execute()
        }
    }

    fun isGuildAdmin(playerId: Uuid, linkedGuildId: Uuid): Boolean {
        return base.withJooq {
            select(GUILDROLES.IS_ADMIN)
                .from(GUILDROLES)
                .where(
                    GUILDROLES.PLAYER_ID.eq(playerId),
                    GUILDROLES.GUILD_ID.eq(linkedGuildId)
                )
                .fetchOne()?.value1() ?: false
        }
    }

    fun isGuildDm(playerId: Uuid, linkedGuildId: Uuid): Boolean {
        return base.withJooq {
            select(GUILDROLES.IS_DUNGEON_MASTER)
                .from(GUILDROLES)
                .where(
                    GUILDROLES.PLAYER_ID.eq(playerId),
                    GUILDROLES.GUILD_ID.eq(linkedGuildId)
                )
                .fetchOne()?.value1() ?: false
        }
    }

    fun getGuildMemberships(playerId: Uuid): List<GuildMembership> {
        return base.withJooq {
            select().from(GUILDROLES).join(GUILDS).onKey()
                .where(GUILDROLES.PLAYER_ID.eq(playerId))
                .fetch {
                    GuildMembership(
                        LinkedGuild(it[GUILDS.ID]!!, it[GUILDS.NAME]!!, it[GUILDS.DISCORD_ID]!!),
                        it[GUILDROLES.IS_ADMIN]!!,
                        it[GUILDROLES.IS_DUNGEON_MASTER]!!
                    )
                }
        }
    }

    fun setGuildMembership(
        playerId: Uuid,
        guildId: Uuid,
        isAdmin: Boolean,
        isDungeonMaster: Boolean
    ) {
        base.withJooq {
            insertInto(
                GUILDROLES,
                GUILDROLES.PLAYER_ID,
                GUILDROLES.GUILD_ID,
                GUILDROLES.IS_ADMIN,
                GUILDROLES.IS_DUNGEON_MASTER
            )
                .values(playerId, guildId, isAdmin, isDungeonMaster)
                .onDuplicateKeyUpdate()
                .set(GUILDROLES.IS_ADMIN, isAdmin)
                .set(GUILDROLES.IS_DUNGEON_MASTER, isDungeonMaster)
                .execute()
        }
    }




}
