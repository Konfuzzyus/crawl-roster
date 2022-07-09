package org.codecranachan.roster.api

import com.benasher44.uuid.Uuid
import org.codecranachan.roster.Guild
import org.codecranachan.roster.repo.Repository

class Permissions(repo: Repository) {

    fun hasAdminRightsForGuild(playerId: Uuid, guild: Guild): Boolean {
        return true
    }

}