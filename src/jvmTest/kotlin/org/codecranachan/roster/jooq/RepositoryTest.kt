package org.codecranachan.roster.jooq

import org.codecranachan.roster.repo.Repository
import org.junit.jupiter.api.BeforeEach

class RepositoryTest {
    val repo = Repository()

    @BeforeEach
    fun setUp() {
        repo.migrate()
    }
}