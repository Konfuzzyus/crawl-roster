package org.codecranachan.roster.api

import com.benasher44.uuid.Uuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.codecranachan.roster.Event
import org.codecranachan.roster.EventRegistration
import org.codecranachan.roster.TableHosting
import org.codecranachan.roster.repo.Repository
import org.codecranachan.roster.repo.addEvent
import org.codecranachan.roster.repo.addEventRegistration
import org.codecranachan.roster.repo.addHostedTable
import org.codecranachan.roster.repo.fetchEvent
import org.codecranachan.roster.repo.isHostingForEvent
import org.codecranachan.roster.repo.isRegisteredForEvent
import org.codecranachan.roster.repo.removeEventRegistration
import org.codecranachan.roster.repo.removeHostedTable
import org.codecranachan.roster.repo.updateEventRegistration

class EventApi(private val repository: Repository) {

    fun install(r: Route) {
        with(r) {
            get("/api/v1/events/{id}") {
                val id = Uuid.fromString(call.parameters["id"])
                val event = repository.fetchEvent(id)
                if (event == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respond(event)
                }
            }

            post("/api/v1/events") {
                val event = call.receive<Event>()
                repository.addEvent(event)
                call.respond(HttpStatusCode.Created)
            }

            post("/api/v1/events/{evtId}/registrations") {
                val evtId = Uuid.fromString(call.parameters["evtId"])
                val reg = call.receive<EventRegistration>()
                if (repository.isHostingForEvent(reg.playerId, evtId)) {
                    call.respond(HttpStatusCode.Conflict)
                } else {
                    repository.addEventRegistration(EventRegistration(reg.id, evtId, reg.playerId))
                    call.respond(HttpStatusCode.Created)
                }
            }

            patch("/api/v1/events/{evtId}/registrations/{plrId}") {
                val evtId = Uuid.fromString(call.parameters["evtId"])
                val plrId = Uuid.fromString(call.parameters["plrId"])
                val reg = call.receive<EventRegistration>()
                repository.updateEventRegistration(evtId, plrId, reg.tableId)
                call.respond(HttpStatusCode.OK)
            }

            delete("/api/v1/events/{evtId}/registrations/{plrId}") {
                val evtId = Uuid.fromString(call.parameters["evtId"])
                val plrId = Uuid.fromString(call.parameters["plrId"])
                repository.removeEventRegistration(evtId, plrId)
                call.respond(HttpStatusCode.OK)
            }


            post("/api/v1/events/{evtId}/tables") {
                val evtId = Uuid.fromString(call.parameters["evtId"])
                val tbl = call.receive<TableHosting>()
                if (repository.isRegisteredForEvent(tbl.dungeonMasterId, evtId)) {
                    call.respond(HttpStatusCode.Conflict)
                } else {
                    repository.addHostedTable(TableHosting(tbl.id, evtId, tbl.dungeonMasterId))
                    call.respond(HttpStatusCode.Created)
                }
            }

            delete("/api/v1/events/{evtId}/tables/{dmId}") {
                val evtId = Uuid.fromString(call.parameters["evtId"])
                val dmId = Uuid.fromString(call.parameters["dmId"])
                repository.removeHostedTable(evtId, dmId)
                call.respond(HttpStatusCode.OK)
            }

        }

    }
}