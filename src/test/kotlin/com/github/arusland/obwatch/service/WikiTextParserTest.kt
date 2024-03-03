package com.github.arusland.obwatch.service

import com.github.arusland.obwatch.util.ResourceUtil
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class WikiTextParserTest {

    @Test
    fun testParser() {
        val result = WikiTextParser().parse(ResourceUtil.readResource("/Junge.wikitext"))
        println(result)

        val result2 = WikiTextParser().parse(ResourceUtil.readResource("/Mutter.wikitext"))
        println(result2)

        val result3 = WikiTextParser().parse(ResourceUtil.readResource("/Maedchen.wikitext"))
        println(result3)

        val result4 = WikiTextParser().parse(ResourceUtil.readResource("/ausmachen.wikitext"))
        println(result4)

        val result5 = WikiTextParser().parse(ResourceUtil.readResource("/prozessen.wikitext"))
        println(result5)
        assertNotNull(result5)
        assertEquals(0, result5?.examples?.size ?: 0)
    }
}
