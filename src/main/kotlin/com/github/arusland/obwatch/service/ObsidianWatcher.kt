package com.github.arusland.obwatch.service

import com.github.arusland.obwatch.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.*


/**
 * Watches for changes in the specified file and create another file with special content.
 *
 * Useful for watching for changes in Obsidian and updating resulting file in another Obsidian view.
 *
 * @param path Path to the file to watch.
 */
class ObsidianWatcher(
    private val path: Path,
    private val dictService: DictService,
    private val wikidataService: WikidataService
) {
    private var lastFileAttributes: BasicFileAttributes
    private val outputFilePath: Path
    private val regexSpace = Regex("[\\s#!,.]+", RegexOption.MULTILINE)
    private val regexDigit = Regex("\\d+")
    private val lastResults = mutableListOf<FoundResult>()

    // contains last tokens and their count
    private val prevTokens = mutableMapOf<String, Int>()

    init {
        require(path.exists()) { "Target path does not exist: $path" }
        require(path.isRegularFile()) { "Target path is not a file: $path" }
        outputFilePath = path.resolveSibling("auto-${path.fileName}")
        lastFileAttributes = path.readAttributes()
        println("Watching for changes in: $path")
        println("Output file: $outputFilePath")
    }

    fun start() {
        while (true) {
            val fileWasChanged = fileWasChanged()
            if (fileWasChanged) {
                processFile()
            }
            Thread.sleep(500)
        }
    }

    private fun processFile() {
        // TODO: file could be too big, so we need to read it in chunks
        val attributes = lastFileAttributes
        log.debug("File was changed: {}, file size: {}", attributes.lastModifiedTime(), attributes.size())
        val text = path.readText()
        val tokens = regexSpace.split(text).filter { token -> token.isNotBlank() && !regexDigit.matches(token) }
            .groupBy { it }.map { it.key to it.value.size }.toMap()
        val newToken = tokens.filter { (newToken, newCount) ->
            prevTokens[newToken]?.let { oldCount -> oldCount < newCount } ?: true
        }.keys.firstOrNull()
        prevTokens.clear()
        prevTokens.putAll(tokens)
        if (newToken != null) {
            searchNewWord(newToken)
        } else {
            log.warn("No words found in the file")
        }
    }

    private fun searchNewWord(lastWord: String, tryAnotherForm: Boolean = true) {
        if (isRussianWord(lastWord)) {
            searchNewWordRussian(lastWord)
        } else {
            searchNewWordGerman(lastWord, tryAnotherForm)
        }
    }

    private fun searchNewWordGerman(newWord: String, tryAnotherForm: Boolean) {
        runBlocking {
            val dictResultDef = async { dictService.lookup(newWord, DictLang.DE_RU) }
            val wikiTextInfoDef = async { wikidataService.search(newWord) }

            val dictResult = dictResultDef.await().let { if (it.def.isEmpty()) null else it }
            val wikiTextInfo = wikiTextInfoDef.await()

            if (dictResult != null && dictResult.def[0].text == newWord || wikiTextInfo != null && wikiTextInfo.isNotEmpty()) {
                addNewWord(newWord, dictResult, wikiTextInfo)
                async { writeResultsToFile() }
            } else if (tryAnotherForm && wikiTextInfo != null && wikiTextInfo.baseForm.isNotBlank()) {
                log.debug(
                    "No definition found for the word: {}, try to find base form: {}",
                    newWord,
                    wikiTextInfo.baseForm
                )
                searchNewWordGerman(wikiTextInfo.baseForm, false)
            } else if (tryAnotherForm) {
                log.debug("No definition found for the word: {}, try to find (de)capitalized form", newWord)
                searchNewWordGerman(
                    if (newWord.first().isUpperCase()) newWord.decapitalize() else newWord.capitalize(),
                    false
                )
            } else {
                if (!hasUmlaut(newWord)) {
                    // maybe it's English word
                    searchNewWordEnglish(newWord)
                } else {
                    log.warn("No definition found for the word (DE): {}", newWord)
                }
            }
        }
    }

    private fun searchNewWordRussian(newWord: String) {
        runBlocking {
            val dictResultDef = dictService.lookup(newWord, DictLang.RU_DE).let { if (it.def.isEmpty()) null else it }
            if (dictResultDef != null) {
                addNewWord(newWord, dictResultDef)
                async { writeResultsToFile() }
            } else {
                log.warn("No definition found for the word (RU): {}", newWord)
            }
        }
    }

    private fun searchNewWordEnglish(newWord: String) {
        runBlocking {
            val dictResultDef = dictService.lookup(newWord, DictLang.EN_DE).let { if (it.def.isEmpty()) null else it }
            if (dictResultDef != null) {
                addNewWord(newWord, dictResultDef)
                async { writeResultsToFile() }
            } else {
                log.warn("No definition found for the word (En): {}", newWord)
            }
        }
    }

    private fun addNewWord(
        newWord: String,
        dictResult: DictResult?,
        wikiTextInfo: WikiTextInfo? = null
    ) {
        synchronized(lastResults) {
            // get term from dictionary api first
            val term = (dictResult?.def?.get(0)?.text ?: wikiTextInfo?.word) ?: newWord
            val result = FoundResult(term, dictResult, wikiTextInfo)
            lastResults.removeIf { it.term.lowercase() == newWord.lowercase() }
            lastResults.add(0, result)
            if (lastResults.size > MAX_WORDS_SIZE) {
                lastResults.removeAt(lastResults.size - 1)
            }
        }
    }

    private fun isRussianWord(word: String): Boolean = word.any { it in 'а'..'я' || it in 'А'..'Я' }

    private fun hasUmlaut(word: String): Boolean = word.any { it in "äöüÄÖÜß" }

    private fun writeResultsToFile() {
        synchronized(lastResults) {
            outputFilePath.bufferedWriter().use { writer ->
                lastResults.forEach { result ->
                    writeResult(writer, result)
                    writer.write("\n----\n")
                }
            }
        }
    }

    private fun writeResult(
        writer: BufferedWriter,
        result: FoundResult
    ) {
        val dictResult = result.result
        writer.write("## Definition of \"${result.term}\"")
        writer.write("\n\n")
        if (dictResult != null && dictResult.def.isNotEmpty()) {
            dictResult.def.forEach { definition ->
                writer.write("**${definition.text}** _\\[${definition.pos}\\]_: ")
                writer.write(definition.tr.map { translationAsString(it) }.joinToString(", ") { it })
                writer.write("\n")
            }
        }

        writeWikiTextInfo(writer, result.wikiTextInfo)
    }

    private fun writeWikiTextInfo(
        writer: BufferedWriter,
        wikiTextInfo: WikiTextInfo?
    ) {
        var info = wikiTextInfo
        while (info != null) {
            if (info.isNotEmpty()) {
                writer.write("\n")
                if (info.hasTable()) {
                    when (info) {
                        is NounInfo -> {
                            writer.write("| |Singular|Plural|\n")
                            writer.write("|--|--|--|\n")
                            info.cases.forEach() { caseInfo ->
                                writer.write("|**${caseInfo.type.value}**|${caseInfo.singularFull}|${caseInfo.pluralFull}|\n")
                            }
                            writer.write("\n")
                        }

                        is VerbInfo -> {
                            writer.write("| |Person|Wortform|\n")
                            writer.write("|--|--|--|\n")
                            writer.write("|**Präterium**|ich|${info.praeterium}|\n")
                            writer.write("|**Perfekt**|${info.hilfsVerb}|${info.partizip2}|\n")
                            // link to flexion
                            writer.write("\n")
                        }

                        is AdjectiveInfo -> {
                            writer.write("|Positiv|Komparativ|Superlativ|\n")
                            writer.write("|--|--|--|\n")
                            writer.write("|**${info.word}**|${info.komparativ}|${info.superlativ}|\n")
                            writer.write("\n")
                        }
                    }
                }

                // draw examples
                if (info.examples.isNotEmpty()) {
                    val exampleSubTitle = getExampleSubTitle(info)
                    writer.write("**Examples**$exampleSubTitle\n")
                    writer.write(info.examples.map { "* $it" }.joinToString("\n") { it })
                    writer.write("\n")
                }

                if (info is VerbInfo) {
                    writer.write("\nAll verb forms: [Flexion](https://de.wiktionary.org/wiki/Flexion:${info.word})\n")
                }
            }
            info = info.next
        }
    }

    private fun getExampleSubTitle(info: WikiTextInfo): String {
        val meaning = if (info.meanings > 1) "${info.meanings} meanings" else ""
        return listOf(info.type, meaning)
            .filter { it.isNotEmpty() }
            .joinToString(", ", prefix = " (_", postfix = "_)")
    }

    private fun translationAsString(translation: Translation): String {
        return if (translation.syn?.isNotEmpty() == true)
            "${translation.text} (_" + translation.syn.joinToString(", ") { it.text } + "_)"
        else if (translation.mean?.isNotEmpty() == true)
            "${translation.text} (_" + translation.mean.joinToString(", ") { it.text } + "_)"
        else
            translation.text
    }

    /**
     * Return true if file was really changed.
     */
    private fun fileWasChanged(): Boolean {
        // TODO: use file watcher
        val attributes = path.readAttributes<BasicFileAttributes>()

        if ((lastFileAttributes.lastModifiedTime() != attributes.lastModifiedTime() || lastFileAttributes.size() != attributes.size())) {
            // wait for some time after save
            lastFileAttributes = attributes
            return true
        }

        return false
    }

    data class FoundResult(
        val term: String,
        val result: DictResult?,
        val wikiTextInfo: WikiTextInfo?
    ) {
        fun hasResult(): Boolean {
            return result != null || wikiTextInfo != null
        }
    }

    private companion object {
        val log = LoggerFactory.getLogger(ObsidianWatcher::class.java)!!
        const val MAX_WORDS_SIZE = 10
    }
}
