package com.github.arusland.obwatch.service

import com.github.arusland.obwatch.model.WikiTextInfo


class WikiTextParser {
    // == Junge ({{Sprache|Deutsch}}) ==
    private val regexWord = """==\s*(\w+)\s*\(.*\)\s*==""".toRegex()
    // === {{Wortart|Substantiv|Deutsch}}, {{f}}, ''Mütter'' ===
    // === {{Wortart|Substantiv|Deutsch}}, {{m}} ===
    // === {{Wortart|Verb|Deutsch}} ===
    private val regexType = """===\s*\{\{Wortart\|(\w+)\|(\w+)\}\}""".toRegex()

    fun parse(wikiText: String): WikiTextInfo? {
        val word = regexWord.find(wikiText)?.groupValues?.get(1)
        val type = regexType.find(wikiText)?.groupValues?.get(1) ?: ""

        if (word == null) {
            return null
        }

        return WikiTextInfo(word, type)
    }
}

