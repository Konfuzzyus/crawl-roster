package org.codecranachan.roster.repo

import com.benasher44.uuid.Uuid
import org.codecranachan.roster.Guild
import org.codecranachan.roster.jooq.Tables.LINKEDGUILDS
import org.codecranachan.roster.jooq.tables.records.LinkedguildsRecord

fun Repository.fetchGuild(guildId: Uuid): Guild? {
    return withJooq {
        selectFrom(LINKEDGUILDS).where(LINKEDGUILDS.ID.eq(guildId)).fetchOne()?.toModel()
    }
}

fun Repository.fetchLinkedGuilds(): List<Guild> {
    return withJooq {
        selectFrom(LINKEDGUILDS).fetch().map { it.toModel() }
    }
}

fun Repository.addLinkedGuild(guild: Guild) {
    return withJooq {
        insertInto(LINKEDGUILDS).set(guild.toRecord()).execute()
    }
}

private fun LinkedguildsRecord.toModel(): Guild {
    return Guild(id, name, discordId)
}

private fun Guild.toRecord(): LinkedguildsRecord {
    return LinkedguildsRecord(id, name, discordId)
}