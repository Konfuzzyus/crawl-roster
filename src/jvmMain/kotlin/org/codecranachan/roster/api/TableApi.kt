package org.codecranachan.roster.api;

import com.benasher44.uuid.Uuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.codecranachan.roster.TableDetails
import org.codecranachan.roster.UserSession
import org.codecranachan.roster.repo.Repository
import org.codecranachan.roster.repo.fetchTable
import org.codecranachan.roster.repo.updateHostedTable

class TableApi(private val repository: Repository) {

    fun install(r: Route) {
        with(r) {
            patch("/api/v1/tables/{id}") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    val id = Uuid.fromString(call.parameters["id"])
                    val details = call.receive<TableDetails>()
                    val table = repository.fetchTable(id)
                    if (table?.let { it.dungeonMaster.id } == userSession.playerId) {
                        repository.updateHostedTable(id, details)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Only the DM can update table details")
                    }
                }
            }
        }
    }
}
