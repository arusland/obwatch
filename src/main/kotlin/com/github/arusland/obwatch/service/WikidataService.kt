package com.github.arusland.obwatch.service

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.arusland.obwatch.model.WikiTextInfo
import com.github.arusland.obwatch.util.JsonUtil
import com.github.arusland.obwatch.util.XmlUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.nio.file.Path

class WikidataService(private val cachePath: Path) {
    private val client = OkHttpClient()

    fun search(term: String): WikiTextInfo? {
        val query = URLEncoder.encode(term, "UTF-8")
        val url = "https://de.wiktionary.org/w/api.php?action=query&titles=$query&format=json&export=true"

        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            log.error("Failed to lookup word: {}, resp: {}", term, response)
            return null
        }

        val json = response.body!!.string()
        val wikidataResponse = JsonUtil.fromJson(json, WikidataResponse::class.java)
        val export = wikidataResponse.query.export.content
        val info = XmlUtil.parseXml(export)
        val wikiText = info.page?.revision?.text?.content ?: return null
        val wikiTextInfo = WikiTextParser().parse(wikiText)

        return wikiTextInfo
    }

    data class WikidataResponse(val query: WikidataQuery)

    data class WikidataQuery(val pages: Map<String, WikidataPage>, val export: WikidataExport)

    data class WikidataPage(val pageId: Int, val title: String = "")

    data class WikidataExport(@JsonProperty("*") val content: String)

    private companion object {
        private val log = LoggerFactory.getLogger(YandexDictService::class.java)!!
    }
}
