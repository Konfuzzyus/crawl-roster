package org.codecranachan.roster.bot

import com.x5.template.Theme
import org.codecranachan.roster.query.EventQueryResult
import org.codecranachan.roster.query.ResolvedTable

class MessageTemplates {
    companion object {
        private val theme = Theme("themes", "", "cmsg")

        fun eventMessageContent(result: EventQueryResult): String {
            val c = theme.makeChunk("event#open")
            c.set("event", result.event)
            c.set("tables", result.tables.values)
            c.set("table_count", result.tables.size)
            c.set("table_space", result.tableSpace)
            c.set("registrations", result.registrations)
            c.set("player_count", result.playerCount)

            return c.toString()
        }

        fun openTableMessageContent(table: ResolvedTable): String {
            return ""
        }

        fun closedTableMessageContent(table: ResolvedTable): String {
            return ""
        }
    }
}