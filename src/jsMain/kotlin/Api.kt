import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import org.codecranachan.roster.Player
import org.codecranachan.roster.PlayerListing
import org.codecranachan.roster.UserIdentity

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

suspend fun fetchUserId(): UserIdentity {
    return client.get("/auth/user").body()
}
