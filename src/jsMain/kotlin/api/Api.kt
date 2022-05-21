package api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.codecranachan.roster.Identity
import org.codecranachan.roster.Player
import org.codecranachan.roster.PlayerListing
import org.reduxkotlin.Thunk
import reducers.ApplicationState
import reducers.IdentifyUserAction

private val scope = MainScope()

val client = HttpClient(Js) {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun fetchPlayers(): PlayerListing {
    return client.get("/api/v1/players").body()
}

suspend fun addPlayer(player: Player) {
    client.post("/api/v1/players") {
        contentType(ContentType.Application.Json)
        setBody(player)
    }
}

suspend fun fetchUserId(): Identity? {
    return try {
        client.get("/api/v1/me").body()
    } catch (e: Exception) {
        null
    }
}

fun updateUserId(): Thunk<ApplicationState> = { dispatch, _, _ ->
    scope.launch {
        val result = fetchUserId()
        dispatch(IdentifyUserAction(result))
    }
}

fun signUpPlayer(p: Player): Thunk<ApplicationState> = { dispatch, _, _ ->
    scope.launch {
        addPlayer(p)
        val result = fetchUserId()
        dispatch(IdentifyUserAction(result))
    }
}
