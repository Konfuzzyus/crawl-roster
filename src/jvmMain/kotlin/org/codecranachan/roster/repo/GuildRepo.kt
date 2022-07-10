package org.codecranachan.roster.repo

import com.benasher44.uuid.Uuid
import org.codecranachan.roster.Guild
import org.codecranachan.roster.jooq.Tables.GUILDS
import org.codecranachan.roster.jooq.tables.records.GuildsRecord

fun Repository.fetchGuild(guildId: Uuid): Guild? {
    return withJooq {
        selectFrom(GUILDS).where(GUILDS.ID.eq(guildId)).fetchOne()?.toModel()
    }
}

fun Repository.fetchLinkedGuilds(): List<Guild> {
    return withJooq {
        selectFrom(GUILDS).fetch().map { it.toModel() }
    }
}

fun Repository.addLinkedGuild(guild: Guild) {
    return withJooq {
        insertInto(GUILDS).set(guild.toRecord()).execute()
    }
}

private fun GuildsRecord.toModel(): Guild {
    return Guild(id, name, discordId)
}

private fun Guild.toRecord(): GuildsRecord {
    return GuildsRecord(id, name, discordId)
}