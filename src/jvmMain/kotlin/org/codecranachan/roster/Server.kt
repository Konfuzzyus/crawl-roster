package org.codecranachan.roster

import RosterServer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.codecranachan.roster.bot.RosterBot
import org.codecranachan.roster.core.RosterCore

suspend fun main() {
    val core = RosterCore()
    coroutineScope {
        val bot = async { Configuration.botToken?.let { token -> RosterBot(core, token, Configuration.rootUrl).start() } }
        val srv = async { RosterServer(core).start() }
        awaitAll(bot, srv)
    }
}
