package com.github.arusland.obwatch.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class XmlUtilTest {
    @Test
    fun testParseXmlOld() {
        val xml = ResourceUtil.readResource("/Junge.xml")
        val result = XmlUtil.parseXml(xml)
        assertEquals("Junge", result.page?.title)
        val text = result.page?.revision?.text
        assertNotNull(text)
        assertEquals(17526, text?.bytes)
        assertEquals(16885, text?.content?.length)
        assertTrue(text?.content?.startsWith("{{Siehe auch|[[junge]], [[jünge]]}}") == true)
        assertTrue(text?.content?.endsWith("[[Jünger]], [[jung]], [[jungen]], [[Junker]], [[Junkie]], [[Lunge]]}}") == true)
    }

    @Test
    fun testParseXml() {
        val xml = ResourceUtil.readResource("/ich.xml")
        val result = XmlUtil.parseXml(xml)
        assertEquals("ich", result.page?.title)
        val text = result.page?.revision?.text
        assertNotNull(text)
        assertEquals(98004, text?.bytes)
        assertEquals(94824, text?.content?.length)
        assertTrue(text?.content?.startsWith("{{Siehe auch|[[Ich]], [[ICH]]}}") == true)
        assertTrue(text?.content?.endsWith("[[lich]], [[mich]], [[nich]]}}") == true)
    }
}