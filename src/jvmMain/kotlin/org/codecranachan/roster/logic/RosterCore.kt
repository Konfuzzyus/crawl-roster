package org.codecranachan.roster.logic

import org.codecranachan.roster.Configuration
import org.codecranachan.roster.logic.events.EventBus
import org.codecranachan.roster.repo.FakeRepoData
import org.codecranachan.roster.repo.Repository

class RosterCore {
    private val repo = Repository()
    val eventBus = EventBus()

    val guildRoster = GuildRosterLogic(repo.guildRepository, Configuration.guildLimit, Configuration.botCoordinates)
    val playerRoster = PlayerRosterLogic(repo.playerRepository, repo.guildRepository)
    val eventCalendar = EventCalendarLogic(eventBus, repo.eventRepository)

    fun initForDevelopment() {
        repo.reset()
        FakeRepoData(repo).insert()
    }

    fun initForProduction() {
        repo.migrate()
    }

}