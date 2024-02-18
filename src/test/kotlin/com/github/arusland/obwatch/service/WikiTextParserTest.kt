package com.github.arusland.obwatch.service

import org.junit.jupiter.api.Test
import java.io.IOException


class WikiTextParserTest {

    @Test
    fun testParser() {
        val result = WikiTextParser().parse(resource("/Junge.wikitext"))
        println(result)

        val result2 = WikiTextParser().parse(resource("/Mutter.wikitext"))
        println(result2)

        val result3 = WikiTextParser().parse(resource("/Maedchen.wikitext"))
        println(result3)

        val result4 = WikiTextParser().parse(resource("/ausmachen.wikitext"))
        println(result4)
    }

    private fun resource(resourceName: String): String {
        WikiTextParserTest::class.java.getResourceAsStream(resourceName).use { stream ->
            return stream?.reader()?.readText() ?: throw IOException("Resource not found: $resourceName")
        }
    }
}