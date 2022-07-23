package org.codecranachan.roster

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable


@Serializable
data class CharacterClass(
    val name: String,
    val level: Int
)

@Serializable
data class Character(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    val dndBeyondId: Int? = null,
    val name: String = "Nameless Hero",
    val classes: List<CharacterClass> = emptyList()
) {
    fun getClassDescription(): String {
        if (classes.isEmpty()) {
            return "Rookie"
        } else {
            val clsTxt = classes.sortedByDescending { it.level }.joinToString("/") { it.name }
            val lvlTxt = classes.sortedByDescending { it.level }.joinToString("/", "(", ")") { it.level.toString() }
            return "$clsTxt $lvlTxt"
        }
    }
}