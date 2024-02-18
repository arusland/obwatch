package com.github.arusland.obwatch.service

import com.github.arusland.obwatch.model.*

/**
 * Parses wiki text and creates [WikiTextInfo] object.
 */
class WikiTextParser {
    // == Junge ({{Sprache|Deutsch}}) ==
    private val regexWord = """==\s*([^(]+?)\s*\(.*\)\s*==""".toRegex()

    // === {{Wortart|Substantiv|Deutsch}}, {{f}}, ''Mütter'' ===
    // === {{Wortart|Substantiv|Deutsch}}, {{m}} ===
    // === {{Wortart|Verb|Deutsch}} ===
    private val regexType = """===\s*\{\{Wortart\|(\w+)\|(\w+)\}\}""".toRegex()

    /**
     *  Parses wiki text and creates on of the [WikiTextInfo] object: [NounInfo], [VerbInfo] or null.
     */
    fun parse(wikiText: String): WikiTextInfo? {
        val word = regexWord.find(wikiText)?.groupValues?.get(1)
        val type = regexType.find(wikiText)?.groupValues?.get(1) ?: ""

        if (word == null) {
            return null
        }

        return when (type) {
            "Substantiv" -> {
                val cases = parseCases(wikiText)
                val genus = getTableValue("Genus", wikiText) // |Genus=f
                NounInfo(word, type, genus, cases)
            }

            "Verb" -> {
                val praeterium = getTableValue("Präteritum_ich", wikiText)
                val partizip2 = getTableValue("Partizip II", wikiText)
                val hilfsVerb = getTableValue("Hilfsverb", wikiText)
                VerbInfo(word, type, praeterium, partizip2, hilfsVerb)
            }

            else -> error("Unknown type: $type")
        }
    }

    private fun parseCases(wikiText: String): Map<CaseType, CaseInfo> {
        // |Nominativ Plural=Mütter
        val result = LinkedHashMap<CaseType, CaseInfo>()

        for (caseType in CaseType.entries) {
            val singular = getTableValue("${caseType.value} Singular", wikiText)
            val plural = getTableValue("${caseType.value} Plural", wikiText)
            result[caseType] = CaseInfo(singular, plural)
        }

        return result
    }

    private fun getTableValue(prefix: String, wikiText: String, defVal: String = ""): String {
        val regex = """\|$prefix( \d+)*=\s*([^\n]+)""".toRegex()
        return regex.find(wikiText)?.groupValues?.get(2) ?: defVal
    }
}
