package org.codecranachan.roster.bot

import com.x5.template.Theme
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.query.EventQueryResult
import org.codecranachan.roster.query.TableQueryResult

class MessageTemplates(private val rootUrl: String) {
    companion object {
        private val theme = Theme("themes", "", "cmsg")

    }

    fun eventMessageContent(data: EventQueryResult): String {
        val c = theme.makeChunk("event#open")
        c.set("event", data.event)
        c.set("tables", data.tables.values.toList())
        c.set("table_count", data.tables.size)
        c.set("table_space", data.tableSpace)
        c.set("unseated", data.unseated)
        c.set("player_count", data.playerCount)
        c.set("root_url", rootUrl)

        return c.toString()
    }

    fun openTableMessageContent(data: TableQueryResult): String {
        val c = theme.makeChunk("table#open")
        c.set("table", data.table)
        c.set("dm", data.dm)
        c.set("players", data.players)
        return c.toString()
    }

    fun closedTableMessageContent(dm: Player): String {
        val c = theme.makeChunk("table#closed")
        c.set("dm", dm)
        return c.toString()
    }
}