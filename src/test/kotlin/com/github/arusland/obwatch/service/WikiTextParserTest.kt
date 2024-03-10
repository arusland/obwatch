package com.github.arusland.obwatch.service

import com.github.arusland.obwatch.util.ResourceUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test


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

        val result6 = WikiTextParser().parse(ResourceUtil.readResource("/mein.wikitext"))
        println(result6)
        assertNotNull(result6)
        assertEquals(3, result6?.examples?.size ?: 0)
        assertEquals("Das ist dein Haus; **mein\\[e\\]s** (**gehoben:** das meine) steht hier.", result6?.examples?.get(1))
    }
}
