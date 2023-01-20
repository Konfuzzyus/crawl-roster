package org.codecranachan.roster.core

import assertk.assertThat
import assertk.assertions.contains
import org.codecranachan.roster.core.events.CalendarEventCreated
import org.codecranachan.roster.core.events.RegistrationCreated
import org.codecranachan.roster.core.events.TableCreated
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class EventCalendarLogicTest : CoreLogicTest() {

    @Test
    fun `adding an event to a nonexistent guild should throw an error`() {
        assertThrows<UnknownGuildException> { logic.addEvent(testEvent) }
    }

    @Test
    fun `adding an event publishes a matching calender event created event`() {
        repository.guildRepository.addLinkedGuild(testGuild)
        val captured = eventBus.capture {
            logic.addEvent(testEvent)
        }
        assertThat(captured).contains(CalendarEventCreated(testEvent))
    }

    @Test
    fun `signing up to a nonexistent event should throw an error`() {
        assertThrows<UnknownEventException> {
            logic.addPlayerRegistration(UUID.randomUUID(), testPlayer.id)
        }
    }

    @Test
    fun `signing up a nonexistent player to an event should throw an error`() {
        repository.guildRepository.addLinkedGuild(testGuild)
        repository.eventRepository.addEvent(testEvent)
        assertThrows<UnknownPlayerException> {
            logic.addPlayerRegistration(testEvent.id, testPlayer.id)
        }
    }

    @Test
    fun `signing up an unregistered player to an event should emit an event update`() {
        setupTestEventAndPlayer()
        val captured = eventBus.capture {
            logic.addPlayerRegistration(testEvent.id, testPlayer.id)
        }
        assertThat(captured).contains(RegistrationCreated(Registration(testEvent.id, testPlayer.id)))
    }

    @Test
    fun `signing up an already registered player to an event should throw an error`() {
        setupTestEventAndPlayer()
        repository.eventRepository.addRegistration(testRegistration)
        assertThrows<PlayerAlreadyRegistered> {
            logic.addPlayerRegistration(testEvent.id, testPlayer.id)
        }
    }

    @Test
    fun `hosting a table for an unknown event should throw an error`() {
        assertThrows<UnknownEventException> {
            logic.addDmRegistration(testEvent.id, testPlayer.id)
        }
    }

    @Test
    fun `hosting a table with a nonexistent player should throw an error`() {
        repository.guildRepository.addLinkedGuild(testGuild)
        repository.eventRepository.addEvent(testEvent)
        assertThrows<UnknownPlayerException> {
            logic.addDmRegistration(testEvent.id, testPlayer.id)
        }
    }

    @Test
    fun `hosting a table should emit a matching event`() {
        setupTestEventAndPlayer()
        val captured = eventBus.capture {
            logic.addDmRegistration(testEvent.id, testPlayer.id)
        }
        assertThat(captured).contains(TableCreated(Table(testEvent.id, testPlayer.id)))
    }

    @Test
    fun `should throw an error if a hosting player tries to register for play`() {
        setupTestEventAndPlayer()
        logic.addDmRegistration(testEvent.id, testPlayer.id)
        assertThrows<PlayerAlreadyHosting> {
            logic.addPlayerRegistration(testEvent.id, testPlayer.id)
        }
    }

    @Test
    fun `should throw an error if a registered player tries to host a table`() {
        setupTestEventAndPlayer()
        logic.addPlayerRegistration(testEvent.id, testPlayer.id)
        assertThrows<PlayerAlreadyRegistered> {
            logic.addDmRegistration(testEvent.id, testPlayer.id)
        }
    }
}