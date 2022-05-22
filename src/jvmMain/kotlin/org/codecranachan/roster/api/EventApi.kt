package org.codecranachan.roster.api

import com.benasher44.uuid.Uuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.codecranachan.roster.Event
import org.codecranachan.roster.repo.Repository
import org.codecranachan.roster.repo.addEvent
import org.codecranachan.roster.repo.fetchEvent
import org.codecranachan.roster.repo.removeEventRegistration

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

            delete("/api/v1/events/{evtId}/registrations/{regId}") {
                val registrationId = Uuid.fromString(call.parameters["regId"])
                repository.removeEventRegistration(registrationId)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}