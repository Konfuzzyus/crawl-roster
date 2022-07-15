package org.codecranachan.roster

import RosterServer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun main() {
    val core = RosterCore()
    coroutineScope {
        listOf(
            async { RosterBot(core).start() },
            async { RosterServer(core).start() }
        )
    }.awaitAll()
}
