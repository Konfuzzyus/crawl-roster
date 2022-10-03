package org.codecranachan.roster.logic

import com.benasher44.uuid.Uuid
import org.codecranachan.roster.LinkedGuild

class GuildAlreadyLinkedException : RuntimeException()

interface GuildRepository {
    fun getLinkedGuild(id: Uuid) : LinkedGuild?

    fun getLinkedGuilds() : List<LinkedGuild>

    @kotlin.jvm.Throws(GuildAlreadyLinkedException::class)
    fun addLinkedGuild(linkedGuild: LinkedGuild)
}