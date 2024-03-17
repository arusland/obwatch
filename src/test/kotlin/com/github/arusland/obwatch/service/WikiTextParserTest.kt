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
        assertEquals(
            "Das ist dein Haus; **mein\\[e\\]s** (**gehoben:** das meine) steht hier.",
            result6?.examples?.get(1)
        )

        val result7 = WikiTextParser().parse(ResourceUtil.readResource("/relevant.wikitext"))
        println(result7)
        assertNotNull(result7)
        assertEquals(4, result7?.examples?.size ?: 0)
        assertEquals(
            "„Es liegt nahe, sich zu fragen, ob man als Nichtbeteiligter in Bezug auf Extrembergsteigen überhaupt eine Meinung entwickeln kann, die **relevant** ist, als nie annähernd in diese Situationen Hineinversetzter.“",
            result7?.examples?.get(3)
        )

        val result8 = WikiTextParser().parse(ResourceUtil.readResource("/prägen.wikitext"))
        println(result8)
        assertNotNull(result8)
        assertEquals(9, result8?.examples?.size ?: 0)
        assertEquals(
            "Descartes **prägte** den berühmten Ausspruch „cogito ergo sum“ – „Ich denke, also bin ich“.",
            result8?.examples?.get(6)
        )

        val result9 = WikiTextParser().parse(ResourceUtil.readResource("/fool.wikitext"))
        println(result9)
        assertNotNull(result9)
        assertEquals(2, result9?.examples?.size ?: 0)
        assertEquals(
            "Don't be a **fool!** (Sei kein **Dummkopf!**)",
            result9?.examples?.get(0)
        )
    }

    @Test
    fun testParserEin() {
        val info = WikiTextParser().parse(ResourceUtil.readResource("/ein.wikitext"))!!
        println(info)
        assertEquals("Artikel", info.type)
        val info2 = info.next!!
        println(info2)
        assertEquals("Numerale", info2.type)
        val info3 = info2.next!!
        println(info3)
        assertEquals("Indefinitpronomen", info3.type)
        val info4 = info3.next!!
        println(info4)
        assertEquals("Adverb", info4.type)
        assertEquals(
            "Tretet **ein,** meine Dame. (eintreten)",
            info4?.examples?.get(0)
        )
        val info5 = info4.next!!
        println(info5)
        assertEquals("Adjektiv", info5.type)
        assertEquals(
            "Ist der Schalter **ein** oder aus? (Ist der Schalter ein- oder ausgeschaltet?)",
            info5?.examples?.get(0)
        )
    }
}
