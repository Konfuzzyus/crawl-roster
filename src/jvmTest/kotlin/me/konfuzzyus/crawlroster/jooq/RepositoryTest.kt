package me.konfuzzyus.crawlroster.jooq

import me.konfuzzyus.crawlroster.Repository
import me.konfuzzyus.crawlroster.jooq.tables.records.HeroRecord
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class RepositoryTest {
    val repo = Repository()

    @Test
    fun accessAHero() {
        repo.migrate()
        repo.addHero(HeroRecord(
            UUID.randomUUID(),
            "Erwin the Great",
            1
        ))
        assertEquals(1, repo.getHeroes().size)
    }
}