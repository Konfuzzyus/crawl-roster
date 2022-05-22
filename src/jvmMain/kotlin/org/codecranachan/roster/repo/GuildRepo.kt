package org.codecranachan.roster.repo

import org.codecranachan.roster.Guild
import org.codecranachan.roster.jooq.Tables
import org.codecranachan.roster.jooq.tables.records.LinkedguildsRecord

fun Repository.fetchLinkedGuilds(): List<Guild> {
    return withJooq {
        selectFrom(Tables.LINKEDGUILDS).fetch().map { it.toModel() }
    }
}

fun Repository.addLinkedGuild(guild: Guild) {
    return withJooq {
        insertInto(Tables.LINKEDGUILDS).set(guild.toRecord()).execute()
    }
}

private fun LinkedguildsRecord.toModel(): Guild {
    return Guild(id, name, discordId)
}

private fun Guild.toRecord(): LinkedguildsRecord {
    return LinkedguildsRecord(id, name, discordId)
}