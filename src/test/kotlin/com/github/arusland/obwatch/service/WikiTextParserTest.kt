package com.github.arusland.obwatch.service

import com.github.arusland.obwatch.model.CaseInfo
import com.github.arusland.obwatch.model.CaseType
import com.github.arusland.obwatch.model.Genus
import com.github.arusland.obwatch.model.NounInfo
import com.github.arusland.obwatch.util.ResourceUtil
import org.junit.jupiter.api.Assertions.*
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
        assertNull(result9)

        val result10 = WikiTextParser().parse(ResourceUtil.readResource("/lo.wikitext"))
        assertNull(result10)
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

    @Test
    fun testParseCases_Hoeren() {
        val info = WikiTextParser().parse(ResourceUtil.readResource("/Hören.wikitext"))!!
        println(info)
        val noun = info as NounInfo
        assertEquals(CaseInfo(CaseType.NOMINATIV, Genus.NEUTRUM, "Hören", "-"), noun.cases[0])
        assertEquals(CaseInfo(CaseType.GENITIV, Genus.NEUTRUM, "Hörens", "-"), noun.cases[1])
        assertEquals(CaseInfo(CaseType.DATIV, Genus.NEUTRUM, "Hören", "-"), noun.cases[2])
        assertEquals(CaseInfo(CaseType.AKKUSATIV, Genus.NEUTRUM, "Hören", "-"), noun.cases[3])
    }

    @Test
    fun testParseCases_Rufen() {
        val info = WikiTextParser().parse(ResourceUtil.readResource("/Rufen.wikitext"))!!
        println(info)
        val noun = info as NounInfo
        assertEquals(CaseInfo(CaseType.NOMINATIV, Genus.NEUTRUM, "Rufen", "-"), noun.cases[0])
        assertEquals(CaseInfo(CaseType.GENITIV, Genus.NEUTRUM, "Rufens", "-"), noun.cases[1])
        assertEquals(CaseInfo(CaseType.DATIV, Genus.NEUTRUM, "Rufen", "-"), noun.cases[2])
        assertEquals(CaseInfo(CaseType.AKKUSATIV, Genus.NEUTRUM, "Rufen", "-"), noun.cases[3])
    }

    @Test
    fun testParseMeanings() {
        val info = WikiTextParser().parse(ResourceUtil.readResource("/Resilienz.wikitext"))!!
        println(info)
        assertNotNull(info)
        assertTrue(info.meanings.isNotEmpty(), "Meanings should not be empty")
        assertEquals(2, info.meanings.size)
        assertEquals("fachsprachlich, Physik, Mechanik, Festkörpermechanik, Werkstoffmechanik, Zahnmedizin: Fähigkeit elastischen Materials, nach starker Verformung in den Ausgangszustand zurückzukehren", info.meanings[0])
        assertEquals("Fähigkeit von Lebewesen, ökonomischen oder sonstigen Systemen, sich gegen erheblichen Druck von außen selbst zu behaupten, äußeren Störungen standzuhalten", info.meanings[1])
    }

    @Test
    fun testParseSynonyms() {
        val info = WikiTextParser().parse(ResourceUtil.readResource("/Mutter.wikitext"))!!
        println(info)
        assertNotNull(info)
        assertTrue(info.synonyms.isNotEmpty(), "Synonyms should not be empty")
        assertEquals(3, info.synonyms.size)
        assertEquals("Mama, Mami, Mammi, Mutti, Muttl, Muttel, Muttchen, Ma", info.synonyms[0])
        assertEquals("Muttertier", info.synonyms[1])
        assertEquals("Gussform, Mutterform", info.synonyms[2])
    }

    @Test
    fun testParseSynonyms2() {
        val info = WikiTextParser().parse(ResourceUtil.readResource("/Mut.wikitext"))!!
        println(info)
        assertNotNull(info)
        assertTrue(info.synonyms.isNotEmpty(), "Synonyms should not be empty")
        assertEquals(3, info.synonyms.size)
        assertEquals("Furchtlosigkeit, Kühnheit, Risikobereitschaft, Rückgrat, Tapferkeit, Unerschrockenheit, Verwegenheit, Wagemut, Courage, Eier, Mumm", info.synonyms[0])
        assertEquals("Optimismus, Vertrauen, Zuversicht", info.synonyms[1])
        assertEquals("Charakter, Verfasstheit", info.synonyms[2])
    }

    @Test
    fun testPhrase() {
        val info = WikiTextParser().parse(ResourceUtil.readResource("/etwas aufs Spiel setzen.wikitext"))!!
        println(info)
        assertNotNull(info)
        assertEquals("Redewendung", info.type)
        assertEquals(1, info.meanings.size)
        assertEquals("umgangssprachlich: etwas riskieren, etwas in Gefahr bringen", info.meanings[0])
        assertEquals(2, info.examples.size)
        assertEquals("Mit dieser Kampagne **setzt** die Firma ihren guten Ruf **aufs Spiel.**", info.examples[0])
        assertEquals("Willst du dein Leben **aufs Spiel setzen?**", info.examples[1])
    }
}
