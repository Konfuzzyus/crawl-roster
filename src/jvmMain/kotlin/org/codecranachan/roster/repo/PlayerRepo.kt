package org.codecranachan.roster.repo

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import org.codecranachan.roster.Player
import org.codecranachan.roster.PlayerDetails
import org.codecranachan.roster.TableLanguage
import org.codecranachan.roster.UserIdentity
import org.codecranachan.roster.jooq.Tables.PLAYERS
import org.codecranachan.roster.jooq.tables.records.PlayersRecord

fun Repository.fetchPlayerByDiscordId(id: String): Player? {
    return withJooq {
        selectFrom(PLAYERS).where(PLAYERS.DISCORD_ID.eq(id)).fetchOne()?.asModel()
    }
}

fun Repository.addPlayer(discordIdentity: UserIdentity?): Player {
    return withJooq {
        val record = PlayersRecord(
            uuid4(),
            "Anonymous",
            Repository.encodeLanguages(listOf(TableLanguage.English)),
            discordIdentity?.id,
            discordIdentity?.name,
            discordIdentity?.pictureUrl
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
        }.store()
    }
}

fun PlayersRecord.asModel(): Player {
    return Player(
        id,
        discordName,
        discordAvatar,
        PlayerDetails(
            playerName,
            Repository.decodeLanguages(languages)
        )
    )
}
