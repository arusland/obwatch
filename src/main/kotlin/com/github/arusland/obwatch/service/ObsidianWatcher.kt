package com.github.arusland.obwatch.service

import com.github.arusland.obwatch.util.JsonUtil
import org.slf4j.LoggerFactory
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
class ObsidianWatcher(private val path: Path, private val dictService: DictService) {
    private var lastFileAttributes: BasicFileAttributes
    private val outputFilePath: Path
    private val regexSpace = Regex("[\\s#!,.]+", RegexOption.MULTILINE)

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
                println("File was changed: $path")
                processFile()
            }
            Thread.sleep(1000)
        }
    }

    private fun processFile() {
        // TODO: file could be too big, so we need to read it in chunks
        val attributes = lastFileAttributes
        log.debug("File was changed: {}, file size: {}", attributes.lastModifiedTime(), attributes.size())
        val text = path.readText()
        val tokens = regexSpace.split(text).filter { it.isNotBlank() }.reversed()
        outputFilePath.bufferedWriter().use { writer ->
            val lastWord = tokens.firstOrNull()

            if (lastWord != null) {
                val result = dictService.lookup(lastWord)
                // to markdown
                writer.write("## $lastWord")
                writer.write("\n\n")
                writer.write("```json")
                writer.write("\n")
                writer.write(JsonUtil.toPrettyJson(result))
                writer.write("\n")
                writer.write("```")
            } else {
                log.warn("No words found in the file")
            }
        }
    }

    private fun fileWasChanged(): Boolean {
        // TODO: use file watcher
        val attributes = path.readAttributes<BasicFileAttributes>()

        if (lastFileAttributes.lastModifiedTime() != attributes.lastModifiedTime() || lastFileAttributes.size() != attributes.size()) {
            lastFileAttributes = attributes
            return true
        }

        return false
    }

    private companion object {
        val log = LoggerFactory.getLogger(ObsidianWatcher::class.java)!!
    }
}
