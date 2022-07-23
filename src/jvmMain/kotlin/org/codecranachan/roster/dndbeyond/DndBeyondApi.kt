package org.codecranachan.roster.dndbeyond

import RosterServer
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable


@Serializable
data class DndBeyondClassDefinition(
    val name: String
)

@Serializable
data class DndBeyondClass(
    val level: Int,
    val definition: DndBeyondClassDefinition
)

@Serializable
data class DndBeyondCharacter(
    val id: Int,
    val name: String?,
    val classes: List<DndBeyondClass>
)


@Serializable
data class DndBeyondResponse(
    val id: Int,
    val success: Boolean,
    val message: String,
    val data: DndBeyondCharacter
)

suspend fun fetchCharacter(id: Int): DndBeyondResponse? {
    return try {
        RosterServer.httpClient.get("https://character-service.dndbeyond.com/character/v5/character/$id").body()
    } catch (e: Exception) {
        null
    }
}

