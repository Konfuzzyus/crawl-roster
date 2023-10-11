package org.codecranachan.roster.bot

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class BotMessageTest {

    @Test
    fun testTitleExtraction() {
        val text =
            """**04-10-2023**
            The event on WED - 4. October, 2023 is now accepting registrations.
            
            Tables will be set at the usual place and doors will open at the usual time
            To register you can use the buttons on this message or head over to http://localhost:8080.
            
            -- Table lineup ---
            No dungeon masters have signed up
            
            --- We have **0** players attending and enough tables for **0** ---
            **Unseated players**
            All players are seated""".trimIndent()

        assertThat(BotMessage.getTitleFromText(text)).isEqualTo("04-10-2023")
    }
}