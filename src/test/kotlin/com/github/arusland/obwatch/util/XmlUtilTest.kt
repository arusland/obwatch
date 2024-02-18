package com.github.arusland.obwatch.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class XmlUtilTest {
    @Test
    fun testParseXml() {
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
}