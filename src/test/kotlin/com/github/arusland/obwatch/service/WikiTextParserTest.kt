package com.github.arusland.obwatch.service

import org.junit.jupiter.api.Test
import java.io.IOException


class WikiTextParserTest {

    @Test
    fun testParser() {
        WikiTextParser().transformToHtml("MediaWiki", resource("/Junge.wikitext"), System.out)
    }

    private fun resource(resourceName: String): String {
        WikiTextParserTest::class.java.getResourceAsStream(resourceName).use { stream ->
            return stream?.reader()?.readText() ?: throw IOException("Resource not found: $resourceName")
        }
    }
}