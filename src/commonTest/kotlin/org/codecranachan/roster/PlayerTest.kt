package org.codecranachan.roster

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerTest {

    @Test
    fun testSerialize() {
        val p = Player("id", "handle")
        val s = Json.encodeToString(p)
        assertEquals("""{"id":"id","handle":"handle"}""", s)
    }

    @Test
    fun testDeserialize() {
        val s = """{"id":"id","handle":"handle"}"""
        val p = Json.decodeFromString<Player>(s)
        assertEquals(Player("id", "handle"), p)
    }

}