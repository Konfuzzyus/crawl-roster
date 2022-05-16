package me.konfuzzyus.roster

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import kotlinx.html.HTML

class ContentApi {

    fun addRoutes(route: Route) {
        route.get("/") {
            call.respondHtml(HttpStatusCode.OK, HTML::index)
        }
        route.static("/static") {
            resources()
        }
    }

}