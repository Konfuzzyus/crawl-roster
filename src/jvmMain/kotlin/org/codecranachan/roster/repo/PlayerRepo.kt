package org.codecranachan.roster.repo

import Configuration
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import org.codecranachan.roster.Character
import org.codecranachan.roster.CharacterClass
import org.codecranachan.roster.DiscordUser
import org.codecranachan.roster.Guild
import org.codecranachan.roster.GuildMembership
import org.codecranachan.roster.Player
import org.codecranachan.roster.PlayerDetails
import org.codecranachan.roster.TableLanguage
import org.codecranachan.roster.jooq.Tables.GUILDROLES
import org.codecranachan.roster.jooq.Tables.GUILDS
import org.codecranachan.roster.jooq.Tables.PLAYERCHARACTERCLASSES
import org.codecranachan.roster.jooq.Tables.PLAYERCHARACTERS
import org.codecranachan.roster.jooq.Tables.PLAYERS
import org.codecranachan.roster.jooq.tables.records.PlayercharacterclassesRecord
import org.codecranachan.roster.jooq.tables.records.PlayercharactersRecord
import org.codecranachan.roster.jooq.tables.records.PlayersRecord

fun Repository.fetchPlayer(playerId: Uuid): Player? {
    return withJooq {
        selectFrom(PLAYERS).where(PLAYERS.ID.eq(playerId)).fetchOne()?.asModel()
    }
}

fun Repository.fetchPlayerByDiscordId(discordId: String): Player? {
    return withJooq {
        selectFrom(PLAYERS).where(PLAYERS.DISCORD_ID.eq(discordId)).fetchOne()?.asModel()
    }
}

fun Repository.addPlayer(discordIdentity: DiscordUser): Player {
    return withJooq {
        val record = PlayersRecord(
            uuid4(),
            "Anonymous",
            Repository.encodeLanguages(listOf(TableLanguage.English)),
            discordIdentity.id,
            discordIdentity.username,
            discordIdentity.getAvatarUrl(),
            0,
            null
        )
        insertInto(PLAYERS).set(record).execute()
        record.asModel()
    }
}

fun Repository.updatePlayer(id: Uuid, playerDetails: PlayerDetails) {
    return withJooq {
        selectFrom(PLAYERS).where(PLAYERS.ID.eq(id)).fetchSingle().apply {
            playerName = playerDetails.name
            languages = Repository.encodeLanguages(playerDetails.languages)
            tierPreference = playerDetails.playTier
            characterPreference = playerDetails.preferredCharacter
        }.store()
    }
}

fun Repository.setGuildMembership(playerId: Uuid, guild: Guild, isAdmin: Boolean, isDungeonMaster: Boolean) {
    withJooq {
        insertInto(
            GUILDROLES,
            GUILDROLES.PLAYER_ID,
            GUILDROLES.GUILD_ID,
            GUILDROLES.IS_ADMIN,
            GUILDROLES.IS_DUNGEON_MASTER
        )
            .values(playerId, guild.id, isAdmin, isDungeonMaster)
            .onDuplicateKeyUpdate()
            .set(GUILDROLES.IS_ADMIN, isAdmin)
            .set(GUILDROLES.IS_DUNGEON_MASTER, isDungeonMaster)
            .execute()
    }
}

fun Repository.getGuildMemberships(playerId: Uuid): List<GuildMembership> {
    return withJooq {
        select().from(GUILDROLES).join(GUILDS).onKey()
            .where(GUILDROLES.PLAYER_ID.eq(playerId))
            .fetch {
                GuildMembership(
                    Guild(it[GUILDS.ID], it[GUILDS.NAME], it[GUILDS.DISCORD_ID]),
                    it[GUILDROLES.IS_ADMIN],
                    it[GUILDROLES.IS_DUNGEON_MASTER]
                )
            }
    }
}

fun Repository.isGuildAdmin(playerId: Uuid, guildId: Uuid): Boolean {
    return withJooq {
        select(GUILDROLES.IS_ADMIN)
            .from(GUILDROLES)
            .where(
                GUILDROLES.PLAYER_ID.eq(playerId),
                GUILDROLES.GUILD_ID.eq(guildId)
            )
            .fetchOne()?.value1() ?: false
    }
}

fun Repository.isGuildDm(playerId: Uuid, guild: Guild): Boolean {
    return withJooq {
        select(GUILDROLES.IS_DUNGEON_MASTER)
            .from(GUILDROLES)
            .where(
                GUILDROLES.PLAYER_ID.eq(playerId),
                GUILDROLES.GUILD_ID.eq(guild.id)
            )
            .fetchOne()?.value1() ?: false
    }
}

fun Repository.addCharacter(playerId: Uuid, character: Character) {
    val characterRecord = PlayercharactersRecord(
        character.id,
        playerId,
        character.dndBeyondId,
        character.name
    )
    val classRecords = character.classes.map {
        PlayercharacterclassesRecord(
            character.id,
            it.level,
            it.name
        )
    }

    withJooq {
        transaction { transactionConfig ->
            characterRecord.attach(transactionConfig)
            characterRecord.store()

            val t = transactionConfig.dsl()
            classRecords.forEach {
                t.insertInto(PLAYERCHARACTERCLASSES).set(it).execute()
            }
        }
    }
}

fun Repository.getPlayerCharacters(playerId: Uuid): List<Character> {
    return withJooq {
        select().from(PLAYERCHARACTERS).join(PLAYERCHARACTERCLASSES).onKey()
            .where(PLAYERCHARACTERS.PLAYER_ID.eq(playerId))
            .orderBy(PLAYERCHARACTERCLASSES.LEVEL.desc())
            .fetchGroups(PLAYERCHARACTERS.ID)
            .map { (id, records) ->
                Character(
                    id = id,
                    dndBeyondId = records.first()[PLAYERCHARACTERS.DND_BEYOND_ID],
                    name = records.first()[PLAYERCHARACTERS.CHARACTER_NAME],
                    classes = records.map { record ->
                        CharacterClass(
                            record[PLAYERCHARACTERCLASSES.NAME],
                            record[PLAYERCHARACTERCLASSES.LEVEL]
                        )
                    }
                )
            }
    }
}

fun PlayersRecord.asModel(): Player {
    return Player(
        id,
        discordName,
        discordAvatar,
        PlayerDetails(
            playerName,
            Repository.decodeLanguages(languages),
            tierPreference,
            characterPreference
        ),
        isServerAdmin = Configuration.isServerAdmin(discordId)
    )
}
