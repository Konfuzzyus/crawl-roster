package org.codecranachan.roster.core

import org.codecranachan.roster.Configuration
import org.codecranachan.roster.core.events.EventBus
import org.codecranachan.roster.repo.Repository
import org.codecranachan.roster.testkit.EntityGenerator

open class RosterLogicException(message: String) : RuntimeException(message)

class RosterCore {
    private val repo = Repository(Configuration.jdbcUri)
    val eventBus = EventBus()

    val guildRoster = GuildRosterLogic(repo.guildRepository, Configuration.guildLimit, Configuration.botCoordinates)
    val playerRoster = PlayerRosterLogic(repo, eventBus)
    val eventCalendar = EventCalendarLogic(repo, eventBus)

    fun initForDevelopment() {
        repo.reset()
    }

    fun injectEntities(vararg entities: Any) {
        EntityGenerator.insertEntities(repo, *entities)
    }

    fun initForProduction() {
        repo.migrate()
    }

}