package org.codecranachan.roster.bot

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.util.Quotes
import org.codecranachan.roster.util.orNull

data class BotMessage(val authorId: Snowflake, val title: String, val text: String = Quotes.getRandom()) {

    companion object {
        private val titleRegex = Regex("^[*]{2}(?<title>[^\n*]+)[*]{2}\n(?<body>.*)$", RegexOption.DOT_MATCHES_ALL)

        fun getMessageTitle(msg: Message) = getTitleFromText(msg.content)
        fun getTitleFromText(text: String) = titleRegex.matchEntire(text)?.let { it.groups["title"]?.value }

        fun getTableTitle(event: Event, dm: Player): String = "${dm.getTableName()} - ${event.getChannelName()}"
    }

    fun asContent(): String {
        return listOf(formattedTitle, text).joinToString("\n")
    }


    private val formattedTitle: String = "**$title**"
}