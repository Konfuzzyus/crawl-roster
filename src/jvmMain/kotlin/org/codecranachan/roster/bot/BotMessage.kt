package org.codecranachan.roster.bot

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import org.codecranachan.roster.util.Quotes
import org.codecranachan.roster.util.orNull

data class BotMessage(val authorId: Snowflake, val title: String, val text: String = Quotes.getRandom()) {

    fun hasSameAuthor(msg: Message): Boolean {
        return msg.author.orNull()?.id == authorId
    }

    fun hasSameTitle(msg: Message): Boolean {
        return msg.content.startsWith(formattedTitle)
    }

    fun asContent(): String {
        return listOf(formattedTitle, text).joinToString("\n")
    }

    private val formattedTitle : String = "**$title**"
}