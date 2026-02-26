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
    private val wikiLinkRegex = """\[\[([^\|\]]+)\|([^\]]+)\]\]""".toRegex()

    // replace simple wikilink [[Fähigkeit]] or [[elastisch]]en by plain text
    private val simpleWikiLinkRegex = """\[\[([^\|\]]+)\]\](\w*)""".toRegex()

    // replace {{K|val1|val2|...}} template by "val1, val2, ...:"
    private val kTemplateRegex = """\{\{K\|([^}]+)\}\}""".toRegex()

    // remove {{xxx|:}} label marker templates (e.g. {{ugs.|:}})
    private val labelMarkerRegex = """\{\{[^|{}]+\|:\}\}""".toRegex()

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
        val synonyms = parseSynonyms(wikiText)
        val baseForm = getTemplateValue("Grundformverweis", wikiText)

        return when (type) {
            "Substantiv" -> {
                val genus = Genus.fromValue(getTableValue("Genus", wikiText)) // |Genus=f
                val cases = parseCases(wikiText, genus)
                NounInfo(word, type, examples, meanings, synonyms, baseForm, next, genus, cases)
            }

            "Verb" -> {
                val praeterium = getTableValue("Präteritum_ich", wikiText)
                val partizip2 = getTableValue("Partizip II", wikiText)
                val hilfsVerb = getTableValue("Hilfsverb", wikiText)
                VerbInfo(word, type, examples, meanings, synonyms, baseForm, next, praeterium, partizip2, hilfsVerb)
            }

            "Adjektiv" -> {
                val komparativ = getTableValue("Komparativ", wikiText)
                val superlativ = getTableValue("Superlativ", wikiText)
                AdjectiveInfo(word, type, examples, meanings, synonyms, baseForm, next, komparativ, superlativ)
            }

            else -> WikiTextInfo(word, type, examples, meanings, synonyms, baseForm, next)
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

    private fun parseExamples(wikiText: String): Pair<List<String>, List<String>> {
        val meanings = parseMeanings(wikiText)
        val examples = mutableMapOf<String, String>()
        val extraExamples = mutableListOf<String>()
        var collecting = false
        val getResult: () -> Pair<List<String>, List<String>> = {
            if (meanings.size == 1) {
                // if only one meaning then add extra examples
                meanings to examples.values + extraExamples.take(5)
            } else {
                meanings to examples.values.toList()
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

    private fun parseMeanings(wikiText: String): List<String> {
        val meanings = mutableMapOf<String, String>()
        var collecting = false
        var lastMeaning: String = ""
        var lastIndex: String = ""
        val addMeaning = {
            if (lastIndex.isNotBlank() && lastMeaning.isNotBlank()) {
                if (!meanings.containsKey(lastIndex)) {
                    meanings[lastIndex] = formatText(lastMeaning.trim())
                }
                lastMeaning = ""
                lastIndex = ""
            }
        }

        wikiText.lines().forEach { line ->
            if (collecting && line.isEmpty()) {
                // when empty line found means that Meanings section is finished
                addMeaning()
                return@forEach
            }
            if (collecting) {
                val match = regexIndex.find(line)
                if (match != null) {
                    // we need whole meaning, that's why we need wait for next one before adding current
                    addMeaning()
                    lastIndex = match.groupValues[1]
                    lastMeaning = match.groupValues[2]
                } else {
                    lastMeaning += " " + line.trim()
                }
            } else if (line.startsWith("{{Bedeutungen}}")) {
                collecting = true
            }
        }
        addMeaning()

        return meanings.values.toList()
    }

    private fun parseSynonyms(wikiText: String): List<String> {
        val synonyms = mutableMapOf<String, String>()
        var collecting = false
        var lastSynonym: String = ""
        var lastIndex: String = ""
        val addSynonym = {
            if (lastIndex.isNotBlank() && lastSynonym.isNotBlank()) {
                if (!synonyms.containsKey(lastIndex)) {
                    synonyms[lastIndex] = formatSynonymText(lastSynonym.trim())
                }
                lastSynonym = ""
                lastIndex = ""
            }
        }

        wikiText.lines().forEach { line ->
            if (collecting && line.isEmpty()) {
                addSynonym()
                collecting = false
                return@forEach
            }
            if (collecting) {
                val match = regexIndex.find(line)
                if (match != null) {
                    addSynonym()
                    lastIndex = match.groupValues[1]
                    lastSynonym = match.groupValues[2]
                } else {
                    lastSynonym += " " + line.trim()
                }
            } else if (line.startsWith("{{Synonyme}}")) {
                collecting = true
            }
        }
        addSynonym()

        return synonyms.values.toList()
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

    private fun formatSynonymText(text: String): String {
        return formatText(text)
            .removeLabelMarker()
            .replace(Regex(";\\s+"), ", ")
            .trim()
    }

    private fun formatText(text: String): String {
        return text.replaceSmall()
            .replaceKTemplate()
            .replaceWikiLink()
            .replaceSimpleWikiLink()
            .replace("''", "**")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .removeSup()
            .removeRef()
    }

    @OptIn(ExperimentalStdlibApi::class)
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
            return wikiText
        }
        return ""
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

    private fun String.replaceSimpleWikiLink(): String = if (this.contains("[["))
        simpleWikiLinkRegex.replace(this, "$1$2")
    else
        this

    private fun String.replaceKTemplate(): String = if (this.contains("{{K|"))
        kTemplateRegex.replace(this) { match ->
            match.groupValues[1].replace("|", ", ") + ":"
        }
    else
        this

    private fun String.removeLabelMarker(): String = if (this.contains("{{"))
        labelMarkerRegex.replace(this, "")
    else
        this
}
