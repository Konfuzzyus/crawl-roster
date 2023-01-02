package org.codecranachan.roster.core

import com.benasher44.uuid.Uuid
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.codecranachan.roster.LinkedGuild
import org.codecranachan.roster.core.events.EventBus
import org.codecranachan.roster.repo.Repository
import org.junit.jupiter.api.BeforeEach

abstract class CoreLogicTest {
    protected val repository = Repository("jdbc:h2:mem:test")
    protected val eventBus = EventBus()
    protected val logic = EventCalendarLogic(repository, eventBus)

    protected val testGuild = LinkedGuild(
        name = "name",
        discordId = "discordId"
    )

    protected val testPlayer = Player(
        discordId = "discordId",
        discordHandle = "discordHandle"
    )
    protected val testEvent = Event(
        guildId = testGuild.id,
        date = LocalDate.parse("2022-06-06")
    )

    protected val testRegistration = Registration(
        eventId = testEvent.id,
        playerId = testPlayer.id
    )

    @BeforeEach
    fun setUp() {
        repository.reset(true)
    }

    protected fun setupTestEventAndPlayer() {
        repository.guildRepository.addLinkedGuild(testGuild)
        repository.eventRepository.addEvent(testEvent)
        repository.playerRepository.addPlayer(testPlayer)
    }


    protected fun insertPlayers(amount: Int): Array<Player> {
        val players = (1..amount).map {
            Player(
                discordId = "discordId#$it",
                discordHandle = "discordHandle#$it"
            )
        }
        players.forEach(repository.playerRepository::addPlayer)
        return players.toTypedArray()
    }

    protected fun insertEvents(guildId: Uuid, amount: Int): Array<Event> {
        val events = (1..amount).map {
            Event(
                guildId = testGuild.id,
                date = LocalDate(2022, 1, 1).plus(it, DateTimeUnit.DAY)
            )
        }
        events.forEach(repository.eventRepository::addEvent)
        return events.toTypedArray()
    }
}