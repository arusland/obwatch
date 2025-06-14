package com.github.arusland.obwatch

import com.github.arusland.obwatch.service.ObsidianWatcher
import com.github.arusland.obwatch.service.WikidataService
import com.github.arusland.obwatch.service.YandexDictService
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: obwatch <targetPath> [outputPath]")
        return
    }
    val targetPath = Paths.get(args[0])
    // cache path in temp dir
    val cachePath = Files.createTempDirectory("obwatch-cache")
    ObsidianWatcher(targetPath, YandexDictService(getApiKey()), WikidataService(cachePath)).start()
}

private fun getApiKey(): String =
    System.getProperty("yandex.dict.api") ?: throw IllegalArgumentException("YANDEX-DICT-API is not set. Please, set -Dyandex.dict.api=<your-api>")
