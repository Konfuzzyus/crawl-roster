package org.codecranachan.roster.bot

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import com.benasher44.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.query.EventQueryResult
import org.junit.jupiter.api.Test

class MessageTemplatesTest {
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
        val result = EventQueryResult(
            expectedEvent,
            emptyList(),
            emptyList(),
            emptyList()
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