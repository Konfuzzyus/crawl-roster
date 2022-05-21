package org.codecranachan.roster.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.codecranachan.roster.Event
import org.codecranachan.roster.repo.*
import java.util.*

class EventCalenderApi(private val repository: Repository) {

    fun install(r: Route) {
        with(r) {
            get("/api/v1/events") {
                call.respond(repository.fetchAllEvents())
            }

            get("/api/v1/events/{id}") {
                val id = UUID.fromString(call.parameters["id"])
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

            post("/api/v1/events/{id}/registrations") {

            }

            delete("/api/v1/events/{evtId}/registrations/{regId}") {
                val registrationId = UUID.fromString(call.parameters["regId"])
                repository.removeEventRegistration(registrationId)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}