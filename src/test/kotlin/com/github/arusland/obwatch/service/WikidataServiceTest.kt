package com.github.arusland.obwatch.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class WikidataServiceTest {
    @Test
    fun testSearch() {
        val service = WikidataService(Paths.get("wikidata-cache"))
        val result = service.search("Junge") ?: fail("Result is null")
        assertEquals("Junge", result.word)
        assertEquals("Substantiv", result.type)
    }
}