package com.github.arusland.obwatch.service

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.arusland.obwatch.model.WikiTextInfo
import com.github.arusland.obwatch.util.JsonUtil
import com.github.arusland.obwatch.util.XmlUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

class WikidataService(private val cachePath: Path) {
    private val client = OkHttpClient()

    @Synchronized
    fun search(term: String): WikiTextInfo? = runBlocking {
        log.debug("Searching for word: {}", term)
        val wikiText = getWikiText(term.trim()) ?: return@runBlocking null
        val wikiTextInfo = WikiTextParser().parse(wikiText)
        launch { removeOldFiles() }

        wikiTextInfo
    }

    private fun getWikiText(term: String): String? {
        val fromCache = getFromCache(term)
        if (fromCache != null) {
            return fromCache
        }
        val query = URLEncoder.encode(term, "UTF-8")
        val url = "https://de.wiktionary.org/w/api.php?action=query&titles=$query&format=json&export=true"

        val request = Request.Builder()
            .url(url)
            .build()

        log.debug("Requesting word: {}", term)
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            log.error("Failed to lookup word: {}, resp: {}", term, response)
            return null
        }

        val json = response.body!!.string()
        val wikidataResponse = JsonUtil.fromJson(json, WikidataResponse::class.java)
        val export = wikidataResponse.query.export.content
        val info = XmlUtil.parseXml(export)
        val wikitext = info.page?.revision?.text?.content
        if (wikitext != null) {
            saveToCache(term, wikitext)
        }
        return wikitext
    }

    private fun getFromCache(term: String): String? {
        val path = cachePath.resolve("$term.wikitext")
        if (!path.toFile().exists() || !path.isRegularFile()) {
            return null
        }
        return path.toFile().readText()
    }

    @Synchronized
    private fun removeOldFiles() {
        // leave only MAX_FILES_IN_CACHE last files
        val files = cachePath.toFile().listFiles()!!.sortedBy { it.lastModified() }
        val toDelete = files.dropLast(MAX_FILES_IN_CACHE)
        toDelete.forEach { it.delete() }
    }

    private fun saveToCache(term: String, content: String) {
        if (!cachePath.exists()) {
            cachePath.toFile().mkdirs()
        }

        val path = cachePath.resolve("$term.wikitext")
        path.toFile().writeText(content)
    }

    data class WikidataResponse(val query: WikidataQuery)

    data class WikidataQuery(val pages: Map<String, WikidataPage>, val export: WikidataExport)

    data class WikidataPage(val pageId: Int, val title: String = "")

    data class WikidataExport(@JsonProperty("*") val content: String)

    private companion object {
        val log = LoggerFactory.getLogger(WikidataService::class.java)!!
        const val MAX_FILES_IN_CACHE = 500
    }
}
