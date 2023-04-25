package org.codecranachan.roster.api

import com.benasher44.uuid.Uuid
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.codecranachan.roster.core.RosterCore
import org.codecranachan.roster.testkit.EntityGenerator

class TestApi(private val core: RosterCore) {
    private val generator = EntityGenerator()

    fun install(r: Route) {
        with(r) {
            post("/api/v1/guilds/{guildId}/crowd-signup") {
                val id = Uuid.fromString(call.parameters["guildId"])
                val calendar = core.eventCalendar.queryCalendar(id)

                if (calendar != null) {
                    val players = EntityGenerator.makeMany(15) { generator.makePlayer() }
                    val dms = EntityGenerator.makeMany(4) { generator.makePlayer() }

                    val regs = calendar.events.flatMap {
                        players.map { pc ->
                            generator.makeRegistration(
                                it.event,
                                pc,
                                dms.random()
                            )
                        } + dms.take(3).map { dm -> generator.makeTable(it.event, dm) }
                    }
                    core.injectEntities(*players, *dms, *regs.toTypedArray())
                }
            }
        }
    }
}

