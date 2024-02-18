package com.github.arusland.obwatch.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class XmlUtilTest {
    @Test
    fun testParseXml() {
        val xml = ResourceUtil.readResource("/Junge.xml")
        val result = XmlUtil.parseXml(xml)
        assertEquals("Junge", result.page?.title)
    }
}