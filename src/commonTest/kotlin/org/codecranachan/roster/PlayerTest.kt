package org.codecranachan.roster

import com.benasher44.uuid.uuid4
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerTest {

    @Test
    fun testSerialize() {
        val id = uuid4()
        val p = Player(id, "myName")
        val s = Json.encodeToString(p)
        assertEquals("""{"id":"$id","discordHandle":"myName"}""", s)
    }

    @Test
    fun testDeserialize() {
        val id = uuid4()
        val s = """{"id":"$id","discordHandle":"myName"}"""
        val p = Json.decodeFromString<Player>(s)
        assertEquals(Player(id, "myName"), p)
    }

}