package org.codecranachan.roster

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.codecranachan.roster.jooq.tables.records.PlayersRecord
import java.util.*

class PlayerApi(val repository: Repository) {

    fun install(r: Route) {
        with(r) {
            get("/api/v1/players") {
                val players = repository.fetchAllPlayers().map(PlayersRecord::asPlayer)
                call.respond(PlayerListing(players))
            }

            get("/api/v1/players/{id}") {
                val id = UUID.fromString(call.parameters["id"])
                val player = repository.fetchPlayer(id)?.let(PlayersRecord::asPlayer)
                if (player == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respond(player)
                }
            }

            post("/api/v1/players") {
                val player = call.receive<Player>()
                repository.addPlayer(player.asRecord())
                call.respond(HttpStatusCode.Created)
            }
        }
    }

}

fun PlayersRecord.asPlayer(): Player {
    return Player(id, handle)
}

fun Player.asRecord(): PlayersRecord {
    return PlayersRecord(id, handle)
}