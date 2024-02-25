package com.github.arusland.obwatch.service

import com.github.arusland.obwatch.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
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
    private val lastResults = mutableListOf<FoundResult>()

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
        val tokens = regexSpace.split(text).filter { it.isNotBlank() }.reversed()
        val lastWord = tokens.firstOrNull()?.trim()

        if (lastWord != null) {
            searchNewWord(lastWord)
        } else {
            log.warn("No words found in the file")
        }
    }

    private fun searchNewWord(lastWord: String, tryBaseForm: Boolean = true) {
        runBlocking {
            val dictResultDef = async { dictService.lookup(lastWord, DictLang.DE_RU) }
            val wikiTextInfoDef = async { wikidataService.search(lastWord) }

            val dictResult = dictResultDef.await().let { if (it.def.isEmpty()) null else it }
            val wikiTextInfo = wikiTextInfoDef.await()

            if (dictResult != null || wikiTextInfo != null && wikiTextInfo.isNotEmpty()) {
                val result = FoundResult(lastWord, dictResult, wikiTextInfo)
                lastResults.removeIf { it.term.lowercase() == lastWord.lowercase() }
                lastResults.add(0, result)
                if (lastResults.size > MAX_WORDS_SIZE) {
                    lastResults.removeAt(lastResults.size - 1)
                }
                outputFilePath.bufferedWriter().use { writer ->
                    lastResults.forEach { result ->
                        writeResult(writer, result)
                        writer.write("----\n\n")
                    }
                }
            } else if (tryBaseForm && wikiTextInfo != null && wikiTextInfo.baseForm.isNotBlank()) {
                log.debug(
                    "No definition found for the word: {}, try to find base form: {}",
                    lastWord,
                    wikiTextInfo.baseForm
                )
                searchNewWord(wikiTextInfo.baseForm, false)
            } else {
                log.warn("No definition found for the word: {}", lastWord)
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
                writer.write("\n\n")
            }
        }

        writeWikiTextInfo(result, writer)

        /*writer.write("```json")
                writer.write("\n")
                writer.write(JsonUtil.toPrettyJson(dictResult))
                writer.write("\n")
                writer.write("```")*/
    }

    private fun writeWikiTextInfo(
        result: FoundResult,
        writer: BufferedWriter
    ) {
        result.wikiTextInfo?.let { info ->
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

            if (info.examples.isNotEmpty()) {
                writer.write("**Examples**\n")
                writer.write(info.examples.map { "* $it" }.joinToString("\n") { it })
                writer.write("\n")
            }

            if (info is VerbInfo) {
                writer.write("\nAll forms: [Flexion](https://de.wiktionary.org/wiki/Flexion:${info.word})\n\n")
            }
        }
    }

    private fun translationAsString(translation: Translation): String {
        return if (translation.syn?.isNotEmpty() == true)
            "${translation.text} (_" + translation.syn.joinToString(", ") { it.text } + "_)"
        else
            translation.text
    }

    private fun fileWasChanged(): Boolean {
        // TODO: use file watcher
        val attributes = path.readAttributes<BasicFileAttributes>()

        if ((lastFileAttributes.lastModifiedTime() != attributes.lastModifiedTime() || lastFileAttributes.size() != attributes.size())
            && calcDiffFromNow(attributes) > WAIT_TIME_AFTER_SAVE
        ) {
            // wait for some time after save
            lastFileAttributes = attributes
            return true
        }

        return false
    }

    private fun calcDiffFromNow(attributes: BasicFileAttributes): Long {
        val lastModified = LocalDateTime.ofInstant(attributes.lastModifiedTime().toInstant(), ZoneId.systemDefault())
        return ChronoUnit.MILLIS.between(lastModified, LocalDateTime.now())
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
        const val WAIT_TIME_AFTER_SAVE = 1000L
        const val MAX_WORDS_SIZE = 10
    }
}
