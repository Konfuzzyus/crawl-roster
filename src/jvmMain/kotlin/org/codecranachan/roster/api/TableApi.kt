package org.codecranachan.roster.api;

import com.benasher44.uuid.Uuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.codecranachan.roster.TableDetails
import org.codecranachan.roster.repo.Repository
import org.codecranachan.roster.repo.updateHostedTable

class TableApi(private val repository: Repository) {

    fun install(r: Route) {
        with(r) {
            patch("/api/v1/tables/{id}") {
                val id = Uuid.fromString(call.parameters["id"])
                val details = call.receive<TableDetails>()
                repository.updateHostedTable(id, details)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
