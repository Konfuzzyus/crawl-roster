package org.codecranachan.roster.repo

import com.benasher44.uuid.Uuid
import org.codecranachan.roster.LinkedGuild
import org.codecranachan.roster.jooq.tables.records.GuildsRecord
import org.codecranachan.roster.jooq.tables.references.GUILDS

class GuildRepository(private val base: Repository) {

    fun getLinkedGuild(id: Uuid): LinkedGuild? {
        return base.withJooq {
            selectFrom(GUILDS).where(GUILDS.ID.eq(id)).fetchOne()?.toModel()
        }
    }

    fun getLinkedGuilds(): List<LinkedGuild> {
        return base.withJooq {
            selectFrom(GUILDS).fetch().map { it.toModel() }
        }
    }

    fun addLinkedGuild(linkedGuild: LinkedGuild) {
        return base.withJooq {
            insertInto(GUILDS).set(linkedGuild.toRecord()).execute()
        }
    }

    fun updateGuild(linkedGuild: LinkedGuild) {
        return base.withJooq {
            update(GUILDS)
                .set(GUILDS.NAME, linkedGuild.name)
                .where(GUILDS.ID.eq(linkedGuild.id))
                .execute()
        }
    }

    private fun GuildsRecord.toModel(): LinkedGuild {
        return LinkedGuild(id!!, name!!, discordId!!)
    }

    private fun LinkedGuild.toRecord(): GuildsRecord {
        return GuildsRecord(id, name, discordId)
    }
}
