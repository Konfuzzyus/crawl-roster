package org.codecranachan.roster.core

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.each
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isIn
import assertk.assertions.isNull
import assertk.assertions.prop
import org.codecranachan.roster.query.EventQueryResult
import org.codecranachan.roster.query.ResolvedTable
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class CalendarQueryTest : CoreLogicTest() {
    @Test
    fun `should return null for unregistered guilds`() {
        val calendar = logic.queryCalendar(testGuild.id)
        assertThat(calendar).isNull()
    }

    @Test
    fun `should return no events for freshly created guilds`() {
        repository.guildRepository.addLinkedGuild(testGuild)
        val calendar = logic.queryCalendar(testGuild.id)
        assertNotNull(calendar)
        assertThat(calendar.linkedGuildId).isEqualTo(testGuild.id)
        assertThat(calendar.events)
    }

    @Test
    fun `should return all registered players for all events`() {
        repository.guildRepository.addLinkedGuild(testGuild)
        val players = insertPlayers(5)
        val events = insertEvents(testGuild.id, 2)
        events.forEach { e -> players.forEach { p -> logic.registerPlayer(e.id, p.id) } }

        val calendar = logic.queryCalendar(testGuild.id)
        assertNotNull(calendar)
        assertThat(calendar.events).hasSize(2)
        assertThat(calendar.events).each {
            it.prop(EventQueryResult::tables).isEmpty()
            it.prop(EventQueryResult::unseated).containsExactlyInAnyOrder(*players)
        }
    }

    @Test
    fun `should return all hosted tables for all events`() {
        repository.guildRepository.addLinkedGuild(testGuild)
        val players = insertPlayers(5)
        val events = insertEvents(testGuild.id, 2)
        events.forEach { e -> players.forEach { p -> logic.hostTable(e.id, p.id) } }

        val calendar = logic.queryCalendar(testGuild.id)
        assertNotNull(calendar)
        assertThat(calendar.events).hasSize(2)
        assertThat(calendar.events).each { evt ->
            evt.prop(EventQueryResult::tables).hasSize(5)
            evt.transform("tables") { it.tables.values }
                .each { tbl ->
                    tbl.prop(ResolvedTable::players).isEmpty()
                    tbl.prop(ResolvedTable::dungeonMaster).isIn(*players)
                }
            evt.prop(EventQueryResult::unseated).isEmpty()
        }
    }
}