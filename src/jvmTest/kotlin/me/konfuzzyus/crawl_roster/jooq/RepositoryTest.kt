package me.konfuzzyus.crawl_roster.jooq

import me.konfuzzyus.crawl_roster.Repository
import me.konfuzzyus.crawl_roster.jooq.tables.records.HeroRecord
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