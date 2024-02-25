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
    private val regexType = """===\s*\{\{Wortart\|([^|]+)\|(\w+)\}\}""".toRegex()

    // :[3] Er macht den Fernseher aus.
    private val regexIndex = """:\[(\d+)\]\s+(.+)""".toRegex()

    // remove <ref>...</ref>
    private val refRegex = """<ref[^>]*>.*?</ref>""".toRegex()

    /**
     *  Parses wiki text and creates on of the [WikiTextInfo] object: [NounInfo], [VerbInfo] or null.
     */
    fun parse(wikiText: String): WikiTextInfo? {
        val word = regexWord.find(wikiText)?.groupValues?.get(1)
        val type = regexType.find(wikiText)?.groupValues?.get(1) ?: ""

        if (word == null) {
            return null
        }

        val examples = parseExamples(wikiText)

        return when (type) {
            "Substantiv" -> {
                val genus = Genus.fromValue(getTableValue("Genus", wikiText)) // |Genus=f
                val cases = parseCases(wikiText, genus)
                NounInfo(word, type, examples, genus, cases)
            }

            "Verb" -> {
                val praeterium = getTableValue("Präteritum_ich", wikiText)
                val partizip2 = getTableValue("Partizip II", wikiText)
                val hilfsVerb = getTableValue("Hilfsverb", wikiText)
                VerbInfo(word, type, examples, praeterium, partizip2, hilfsVerb)
            }

            "Adjektiv" -> {
                val komparativ = getTableValue("Komparativ", wikiText)
                val superlativ = getTableValue("Superlativ", wikiText)
                AdjectiveInfo(word, type, examples, komparativ, superlativ)
            }

            "Deklinierte Form" -> null // ignore

            else -> WikiTextInfo(word, type, examples)
        }
    }

    private fun parseExamples(wikiText: String): List<String> {
        val examples = mutableMapOf<String, String>()
        var collecting = false

        wikiText.lines().forEach { line ->
            if (collecting && line.isEmpty()) {
                return@forEach
            }
            if (collecting) {
                regexIndex.find(line)?.let { match ->
                    val index = match.groupValues[1]
                    if (!examples.containsKey(index)) {
                        val text = match.groupValues[2]
                        examples[index] = formatText(text)
                    }
                }
            } else if (line.startsWith("{{Beispiele}}")) {
                collecting = true
            }
        }

        return examples.values.toList()
    }

    private fun formatText(text: String): String {
        return refRegex.replace(text, "").replace("''", "**")
    }

    private fun parseCases(wikiText: String, genus: Genus): List<CaseInfo> {
        // |Nominativ Plural=Mütter
        val result = mutableListOf<CaseInfo>()

        for (caseType in CaseType.entries) {
            val singular = getTableValue("${caseType.value} Singular", wikiText)
            val plural = getTableValue("${caseType.value} Plural", wikiText)
            result.add(CaseInfo(caseType, genus, singular, plural))
        }

        return result
    }

    private fun getTableValue(prefix: String, wikiText: String, defVal: String = ""): String {
        val regex = """\|$prefix( \d+)*=\s*([^\n]+)""".toRegex()
        return regex.find(wikiText)?.groupValues?.get(2) ?: defVal
    }
}
