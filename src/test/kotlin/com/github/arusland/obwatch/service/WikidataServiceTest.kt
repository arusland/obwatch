package com.github.arusland.obwatch.service

import com.github.arusland.obwatch.model.AdjectiveInfo
import com.github.arusland.obwatch.model.NounInfo
import com.github.arusland.obwatch.model.VerbInfo
import com.github.arusland.obwatch.model.WikiTextInfo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Paths

class WikidataServiceTest {
    private val service = WikidataService(Paths.get("wikidata-cache"))

    @ValueSource(strings = ["Junge", "Mädchen", "Haus"])
    @ParameterizedTest
    fun testSearch_WhenNoun(term: String) {
        val result = service.search(term) ?: fail("Result is null")
        assertEquals(term, result.word)
        assertEquals("Substantiv", result.type)
        assertEquals(NounInfo::class.java, result.javaClass)
        println(result)
    }

    @ValueSource(strings = ["machen", "gehen", "sein"])
    @ParameterizedTest
    fun testSearch_WhenVerb(term: String) {
        val result = service.search(term) ?: fail("Result is null")
        assertEquals(term, result.word)
        assertEquals("Verb", result.type)
        assertEquals(VerbInfo::class.java, result.javaClass)
        println(result)
    }

    @ValueSource(strings = ["das", "die"])
    @ParameterizedTest
    fun testSearch_WhenArtikel(term: String) {
        val result = service.search(term) ?: fail("Result is null")
        assertEquals(term, result.word)
        assertEquals("Artikel", result.type)
        assertEquals(WikiTextInfo::class.java, result.javaClass)
        println(result)
    }

    @ValueSource(strings = ["jung", "schön", "gut", "berufstätig"])
    @ParameterizedTest
    fun testSearch_WhenAdj(term: String) {
        val result = service.search(term) ?: fail("Result is null")
        assertEquals(term, result.word)
        assertEquals("Adjektiv", result.type)
        assertEquals(AdjectiveInfo::class.java, result.javaClass)
        println(result)
    }

    @CsvSource(
        value = [
            "berufstätige,  berufstätig",
            "junge,         jung",
        ]
    )
    @ParameterizedTest
    fun testSearch_WhenDeklinierteForm(term: String, baseForm: String) {
        val result = service.search(term) ?: fail("Result is null")
        assertEquals(term, result.word)
        assertEquals(baseForm, result.baseForm)
        assertEquals("Deklinierte Form", result.type)
        assertEquals(WikiTextInfo::class.java, result.javaClass)
        println(result)
    }

    @CsvSource(
        value = [
            "heute,     Temporaladverb",
            "draußen,   Adverb",
            "abzgl.,    Abkürzung",
        ]
    )
    @ParameterizedTest
    fun testSearch_WhenOther(term: String, type: String) {
        val result = service.search(term) ?: fail("Result is null")
        assertEquals(term, result.word)
        assertEquals(type, result.type)
        assertEquals(WikiTextInfo::class.java, result.javaClass)
        println(result)
    }

    @Test
    fun testSearchWhenBaseForm() {
        val result = service.search("sei") ?: fail("Result is null")
        assertEquals("Konjugierte Form", result.type)
        assertEquals("sein", result.baseForm)
        assertEquals(WikiTextInfo::class.java, result.javaClass)
        assertEquals(0, result.examples.size) { "Examples should be empty, but found ${result.examples}" }
        println(result)
    }

    @ValueSource(strings = ["mädchen"])
    @ParameterizedTest
    fun testSearch_WhenNotFound(term: String) {
        val result = service.search(term)
        assertNull(result)
    }
}