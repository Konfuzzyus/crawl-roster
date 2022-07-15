package org.codecranachan.roster

import org.codecranachan.roster.logic.EventCalendarLogic
import org.codecranachan.roster.logic.GuildRosterLogic
import org.codecranachan.roster.logic.PlayerRosterLogic
import org.codecranachan.roster.repo.FakeRepoData
import org.codecranachan.roster.repo.Repository

class RosterCore {
    private val repo = Repository()

    val guildRoster = GuildRosterLogic(repo.guildRepository, Configuration.guildLimit)
    val playerRoster = PlayerRosterLogic(repo.playerRepository, repo.guildRepository)
    val eventCalendar = EventCalendarLogic(repo.eventRepository)

    fun initForDevelopment() {
        repo.reset()
        FakeRepoData(repo).insert()
    }

    fun initForProduction() {
        repo.migrate()
    }

}