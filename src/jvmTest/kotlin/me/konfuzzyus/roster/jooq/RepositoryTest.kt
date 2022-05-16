package me.konfuzzyus.roster.jooq

import assertk.assertThat
import assertk.assertions.isEmpty
import me.konfuzzyus.roster.Repository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RepositoryTest {
    val repo = Repository()

    @BeforeEach
    fun setUp() {
        repo.migrate()
    }

    @Test
    fun fetchPlayers() {
        val players = repo.fetchAllPlayers()
        assertThat(players).isEmpty()
    }
}