package com.github.arusland.obwatch.util

import com.github.arusland.obwatch.model.DictResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JsonUtilTest {
    @Test
    fun testFromJson() {
        val json = """{"head":{},"def":[{"text":"Röstigraben","tr":[{"text":"Рёштиграбен","fr":10}]}],"nmt_code":200,"code":200}"""
        val result = JsonUtil.fromJson(json, DictResult::class.java)
        assertNotNull(result)
        assertEquals(1, result.def.size)
        assertEquals("Röstigraben", result.def[0].text)
        assertEquals(1, result.def[0].tr.size)
        assertEquals("Рёштиграбен", result.def[0].tr[0].text)
        assertEquals(10, result.def[0].tr[0].fr)
    }
}
