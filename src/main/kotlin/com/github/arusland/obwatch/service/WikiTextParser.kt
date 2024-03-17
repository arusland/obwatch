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
    private val regexIndex = """:\[([\da-z]+)\]\s+(.+)""".toRegex()

    // remove <ref>...</ref>
    private val refRegex = """<ref[^>]*>.*?</ref>""".toRegex()

    // remove <sup>1</sup>
    private val supRegex = """<sup>[^<]+?</sup>""".toRegex()

    // replace wikilink by it's value [[w:René Descartes|Descartes]]
    private val wikiLinkRegex = """\[\[([^\|]+)\|([^\]]+)\]\]""".toRegex()

    // replace small by it's value <small>...</small>
    private val smallRegex = """<small>([^<]+)</small>""".toRegex()

    /**
     *  Parses wiki text and creates on of the [WikiTextInfo] object: [NounInfo], [VerbInfo] or null.
     */
    fun parse(wikiText: String): WikiTextInfo? {
        val wikiText = cutOnlyGerman(wikiText)
        val word = regexWord.find(wikiText)?.groupValues?.get(1) ?: return null
        val parts = splitWikiText(wikiText).reversed()
        var result: WikiTextInfo? = null

        parts.forEach { part ->
            val current = parseInternal(word, part, result)
            if (current != null) {
                result = current
            }
        }

        return result
    }

    private fun parseInternal(word: String, wikiText: String, next: WikiTextInfo?): WikiTextInfo? {
        val type = regexType.find(wikiText)?.groupValues?.get(1) ?: ""

        val (meanings, examples) = parseExamples(wikiText)
        val baseForm = getTemplateValue("Grundformverweis", wikiText)

        return when (type) {
            "Substantiv" -> {
                val genus = Genus.fromValue(getTableValue("Genus", wikiText)) // |Genus=f
                val cases = parseCases(wikiText, genus)
                NounInfo(word, type, examples, meanings, baseForm, next, genus, cases)
            }

            "Verb" -> {
                val praeterium = getTableValue("Präteritum_ich", wikiText)
                val partizip2 = getTableValue("Partizip II", wikiText)
                val hilfsVerb = getTableValue("Hilfsverb", wikiText)
                VerbInfo(word, type, examples, meanings, baseForm, next, praeterium, partizip2, hilfsVerb)
            }

            "Adjektiv" -> {
                val komparativ = getTableValue("Komparativ", wikiText)
                val superlativ = getTableValue("Superlativ", wikiText)
                AdjectiveInfo(word, type, examples, meanings, baseForm, next, komparativ, superlativ)
            }

            else -> WikiTextInfo(word, type, examples, meanings, baseForm, next)
        }
    }

    private fun splitWikiText(wikiText: String): List<String> {
        val result = mutableListOf<String>()
        var index = wikiText.indexOf("=== {{Wortart")
        while (index >= 0) {
            val nextIndex = wikiText.indexOf("=== {{Wortart", index + 1)
            if (nextIndex > 0) {
                result.add(wikiText.substring(index, nextIndex))
            } else {
                result.add(wikiText.substring(index))
            }
            index = nextIndex
        }

        return result
    }

    private fun parseExamples(wikiText: String): Pair<Int, List<String>> {
        val examples = mutableMapOf<String, String>()
        val extraExamples = mutableListOf<String>()
        var collecting = false
        val getResult: () -> Pair<Int, List<String>> = {
            if (examples.size == 1) {
                // if only one meaning then add extra examples
                1 to examples.values + extraExamples.take(5)
            } else {
                examples.size to examples.values.toList()
            }
        }
        var lastExample: String = ""
        var lastIndex: String = ""
        val addExample = {
            if (lastIndex.isNotBlank() && !lastExample.startsWith("{")) {
                // ignore examples with templates, e.g. {{Beispiele fehlen|spr=de}}
                if (!examples.containsKey(lastIndex)) {
                    examples[lastIndex] = formatText(lastExample)
                } else {
                    extraExamples.add(formatText(lastExample))
                }
                lastExample = ""
                lastIndex = ""
            }
        }

        wikiText.lines().forEach { line ->
            if (collecting && line.isEmpty()) {
                // when empty line found means that Examples section is finished
                addExample()
                return getResult()
            }
            if (collecting) {
                val match = regexIndex.find(line)
                if (match != null) {
                    // we need whole example, that's why we need wait for next one before adding current
                    addExample()
                    lastIndex = match.groupValues[1]
                    lastExample = match.groupValues[2]
                } else {
                    lastExample += formatExampleBeforeAdd(line)
                }
            } else if (line.startsWith("{{Beispiele}}")) {
                collecting = true
            }
        }
        addExample()

        return getResult()
    }

    private fun formatExampleBeforeAdd(line: String): String = if (line.startsWith("::")) {
        " (" + line.substring(2) + ")"
    } else
        line

    private fun getTemplateValue(prefix: String, wikiText: String): String {
        // {{Grundformverweis Konj|gehen}}
        val regex = """\{\{\s*$prefix[^\|]*?\|([^\}]+)""".toRegex()
        return regex.find(wikiText)?.groupValues?.get(1) ?: ""
    }

    private fun formatText(text: String): String {
        return text.replaceSmall()
            .replaceWikiLink()
            .replace("''", "**")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .removeSup()
            .removeRef()
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

    private fun cutOnlyGerman(wikiText: String): String {
        val langDe = "({{Sprache|Deutsch"
        val deIndex = wikiText.indexOf(langDe)
        if (deIndex > 0) {
            var index = wikiText.indexOf("({{Sprache|", deIndex + langDe.length)
            if (index > 0) {
                while (index > 0 && wikiText[index] != '\n') {
                    index--
                }
                return wikiText.substring(0, index)
            }
        }
        return wikiText
    }

    private fun getTableValue(prefix: String, wikiText: String, defVal: String = ""): String {
        val regex = """\|$prefix( \d+)*=\s*([^\n]+)""".toRegex()
        return regex.find(wikiText)?.groupValues?.get(2) ?: defVal
    }

    private fun String.removeSup(): String = if (this.contains("sup"))
        supRegex.replace(this, "")
    else
        this

    private fun String.removeRef(): String = if (this.contains("ref"))
        refRegex.replace(this, "")
    else
        this

    private fun String.replaceSmall(): String = if (this.contains("small"))
        smallRegex.replace(this, "$1")
    else
        this

    private fun String.replaceWikiLink(): String  = if (this.contains("[["))
        wikiLinkRegex.replace(this, "$2")
    else
        this
}
