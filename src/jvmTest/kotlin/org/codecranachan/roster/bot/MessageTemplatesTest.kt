package org.codecranachan.roster.bot

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import com.benasher44.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.query.EventQueryResult
import org.codecranachan.roster.testkit.EntityGenerator
import org.junit.jupiter.api.Test

class MessageTemplatesTest {
    private val testGen = EntityGenerator()

    private val expectedEvent = Event(
        guildId = Uuid.randomUUID(),
        date = LocalDate.parse("2022-06-06"),
        details = Event.Details(
            time = LocalTime.parse("18:00:00"),
            location = "MyLocation"
        )
    )

    @Test
    fun renderOpenEventContent() {
        val players = testGen.makeMany(5) { testGen.makePlayer() }
        val dms = testGen.makeMany(3) { testGen.makePlayer() }
        val mysteryDms = testGen.makeMany(2) { testGen.makePlayer() }
        val tables = dms.map { dm -> testGen.makeTable(expectedEvent, dm) }
        val registrations = (dms + mysteryDms).zip(players).map { (dm, pl) -> testGen.makeRegistration(expectedEvent, pl, dm) }

        val result = EventQueryResult(
            expectedEvent,
            registrations,
            tables,
            listOf(*players, *dms, *mysteryDms)
        )

        val content = MessageTemplates.eventMessageContent(result)
        assertThat(content).all {
            contains(expectedEvent.formattedDate)
            contains(expectedEvent.details.location!!)
            contains(expectedEvent.details.time.toString())
            contains(result.playerCount.toString())
            contains(result.tableSpace.toString())
        }

        println(content)
    }
}