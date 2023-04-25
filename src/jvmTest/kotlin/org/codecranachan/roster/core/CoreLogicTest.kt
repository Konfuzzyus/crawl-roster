package org.codecranachan.roster.core

import org.codecranachan.roster.LinkedGuild
import org.codecranachan.roster.core.events.EventBus
import org.codecranachan.roster.repo.Repository
import org.codecranachan.roster.testkit.EntityGenerator
import org.junit.jupiter.api.BeforeEach
import java.util.function.Supplier

abstract class CoreLogicTest {
    protected val repository = Repository("jdbc:h2:mem:test")
    protected val eventBus = EventBus()
    protected val logic = EventCalendarLogic(repository, eventBus)
    protected val testGen = EntityGenerator()

    protected val testGuild = testGen.makeGuild()
    protected val testPlayer = testGen.makePlayer()
    protected val testEvent = testGen.makeEvent(testGuild)
    protected val testRegistration = Registration(eventId = testEvent.id, playerId = testPlayer.id)

    @BeforeEach
    fun setUp() {
        repository.reset(true)
    }

    protected fun setupTestEventAndPlayer() {
        EntityGenerator.insertEntities(repository, testGuild, testPlayer, testEvent)
    }

    private inline fun <reified T : Any> insert(amount: Int, supplier: Supplier<T>): Array<T> {
        val entities = EntityGenerator.makeMany(amount, supplier::get)
        EntityGenerator.insertEntities(repository, *entities)
        return entities
    }

    protected fun insertPlayers(amount: Int): Array<Player> {
        return insert(amount, testGen::makePlayer)
    }

    protected fun insertEvents(amount: Int, guild: LinkedGuild = testGuild): Array<Event> {
        return insert(amount) { testGen.makeEvent(guild) }
    }
}