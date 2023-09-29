package org.codecranachan.roster.bot

import com.x5.template.Theme
import org.codecranachan.roster.query.EventQueryResult
import org.codecranachan.roster.query.ResolvedTable
import org.codecranachan.roster.query.TableQueryResult

class MessageTemplates(private val rootUrl: String) {
    companion object {
        private val theme = Theme("themes", "", "cmsg")

    }

    fun eventMessageContent(result: EventQueryResult): String {
        val c = theme.makeChunk("event#open")
        c.set("event", result.event)
        c.set("tables", result.tables.values)
        c.set("table_count", result.tables.size)
        c.set("table_space", result.tableSpace)
        c.set("registrations", result.registrations)
        c.set("player_count", result.playerCount)
        c.set("root-url", rootUrl)

        return c.toString()
    }

    fun openTableMessageContent(result: TableQueryResult): String {
        val c = theme.makeChunk("table#open")
        c.set("table", result.table)
        c.set("dm", result.dm)
        c.set("players", result.players)
        return c.toString()
    }

    fun closedTableMessageContent(table: TableQueryResult): String {
        val c = theme.makeChunk("table#close")
        return c.toString()
    }
}