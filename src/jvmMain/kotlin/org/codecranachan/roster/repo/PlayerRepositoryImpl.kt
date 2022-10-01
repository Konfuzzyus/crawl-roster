package org.codecranachan.roster.repo

import com.benasher44.uuid.Uuid
import org.codecranachan.roster.GuildMembership
import org.codecranachan.roster.LinkedGuild
import org.codecranachan.roster.Player
import org.codecranachan.roster.PlayerDetails
import org.codecranachan.roster.jooq.Tables.GUILDROLES
import org.codecranachan.roster.jooq.Tables.GUILDS
import org.codecranachan.roster.jooq.Tables.PLAYERS
import org.codecranachan.roster.jooq.tables.records.PlayersRecord
import org.codecranachan.roster.logic.PlayerRepository

class PlayerRepositoryImpl(private val base: Repository) : PlayerRepository {
    override fun getPlayer(playerId: Uuid): Player? {
        return base.withJooq {
            selectFrom(PLAYERS).where(PLAYERS.ID.eq(playerId)).fetchOne()?.asModel()
        }
    }

    override fun getPlayerByDiscordId(discordId: String): Player? {
        return base.withJooq {
            selectFrom(PLAYERS).where(PLAYERS.DISCORD_ID.eq(discordId)).fetchOne()?.asModel()
        }
    }

    override fun addPlayer(player: Player) {
        return base.withJooq {
            insertInto(PLAYERS).set(player.asRecord()).execute()
        }
    }

    override fun updatePlayer(playerId: Uuid, details: PlayerDetails) {
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

    override fun isGuildAdmin(playerId: Uuid, linkedGuildId: Uuid): Boolean {
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

    override fun isGuildDm(playerId: Uuid, linkedGuildId: Uuid): Boolean {
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

    override fun getGuildMemberships(playerId: Uuid): List<GuildMembership> {
        return base.withJooq {
            select().from(GUILDROLES).join(GUILDS).onKey()
                .where(GUILDROLES.PLAYER_ID.eq(playerId))
                .fetch {
                    GuildMembership(
                        LinkedGuild(it[GUILDS.ID], it[GUILDS.NAME], it[GUILDS.DISCORD_ID]),
                        it[GUILDROLES.IS_ADMIN],
                        it[GUILDROLES.IS_DUNGEON_MASTER]
                    )
                }
        }
    }

    override fun setGuildMembership(
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


    private fun Player.asRecord(): PlayersRecord {
        return PlayersRecord(
            id,
            details.name,
            Repository.encodeLanguages(details.languages),
            discordId,
            discordHandle,
            avatarUrl,
            details.playTier
        )
    }

    private fun PlayersRecord.asModel(): Player {
        return Player(
            id,
            discordId,
            discordName,
            discordAvatar,
            PlayerDetails(
                playerName,
                Repository.decodeLanguages(languages),
                tierPreference
            )
        )
    }

}
