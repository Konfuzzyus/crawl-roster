package org.codecranachan.roster.repo

import com.benasher44.uuid.Uuid
import org.codecranachan.roster.Player
import org.codecranachan.roster.UserIdentity
import org.codecranachan.roster.jooq.Tables.PLAYERS
import org.codecranachan.roster.jooq.tables.records.PlayersRecord

fun Repository.fetchPlayerByGoogleId(id: String): Player? {
    return withJooq {
        selectFrom(PLAYERS).where(PLAYERS.GOOGLE_ID.eq(id)).fetchOne()?.asModel()
    }
}

fun Repository.fetchPlayerByDiscordId(id: String): Player? {
    return withJooq {
        selectFrom(PLAYERS).where(PLAYERS.DISCORD_ID.eq(id)).fetchOne()?.asModel()
    }
}

fun Repository.fetchAllPlayers(): List<Player> {
    return withJooq {
        selectFrom(PLAYERS).fetch().toList().map { it.asModel() }
    }
}

fun Repository.fetchPlayer(id: Uuid): PlayersRecord? {
    return withJooq {
        selectFrom(PLAYERS).where(PLAYERS.ID.eq(id)).fetchOne()
    }
}

fun Repository.addPlayer(player: Player, discordIdentity: UserIdentity?, googleIdentity: UserIdentity?) {
    withJooq {
        val record = PlayersRecord(
            player.id,
            player.name,
            discordIdentity?.name,
            discordIdentity?.id,
            googleIdentity?.id
        )
        insertInto(PLAYERS).set(record).execute()
    }
}

fun Repository.setPlayerDiscordInfo(playerId: Uuid, identity: UserIdentity) {
    withJooq {
        update(PLAYERS)
            .set(
                mapOf(
                    PLAYERS.DISCORD_ID to identity.id,
                    PLAYERS.DISCORD_NAME to identity.name
                )
            )
            .where(PLAYERS.ID.eq(playerId))
            .execute()
    }
}

fun Repository.setPlayerGoogleInfo(playerId: Uuid, identity: UserIdentity) {
    withJooq {
        update(PLAYERS)
            .set(mapOf(PLAYERS.GOOGLE_ID to identity.id))
            .where(PLAYERS.ID.eq(playerId))
            .execute()
    }
}

fun PlayersRecord.asModel(): Player {
    return Player(id, playerName, discordName)
}
