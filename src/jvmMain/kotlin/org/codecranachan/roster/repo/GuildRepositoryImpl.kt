package org.codecranachan.roster.repo

import com.benasher44.uuid.Uuid
import org.codecranachan.roster.LinkedGuild
import org.codecranachan.roster.jooq.Tables.GUILDS
import org.codecranachan.roster.jooq.tables.records.GuildsRecord
import org.codecranachan.roster.logic.GuildRepository

class GuildRepositoryImpl(private val base: Repository) : GuildRepository {

    override fun getLinkedGuild(id: Uuid): LinkedGuild? {
        return base.withJooq {
            selectFrom(GUILDS).where(GUILDS.ID.eq(id)).fetchOne()?.toModel()
        }
    }

    override fun getLinkedGuilds(): List<LinkedGuild> {
        return base.withJooq {
            selectFrom(GUILDS).fetch().map { it.toModel() }
        }
    }

    override fun addLinkedGuild(linkedGuild: LinkedGuild) {
        return base.withJooq {
            insertInto(GUILDS).set(linkedGuild.toRecord()).execute()
        }
    }

    private fun GuildsRecord.toModel(): LinkedGuild {
        return LinkedGuild(id, name, discordId)
    }

    private fun LinkedGuild.toRecord(): GuildsRecord {
        return GuildsRecord(id, name, discordId)
    }
}
