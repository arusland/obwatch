package com.github.arusland.obwatch.service

import com.github.arusland.obwatch.model.DictResult
import com.github.arusland.obwatch.util.JsonUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory

class YandexDictService(private val apiKey: String) : DictService {
    private val client = OkHttpClient()

    override fun lookup(word: String): DictResult {
        val word = word.trim()
        val url = "https://dictionary.yandex.net/api/v1/dicservice.json/lookup?key=$apiKey&lang=en-ru&text=$word"

        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            log.error("Failed to lookup word: {}, resp: {}", word, response)
            return DictResult(emptyList())
        }

        val json = response.body!!.string()
        val result = JsonUtil.fromJson(json, DictResult::class.java)
        return result
    }

    private companion object {
        private val log = LoggerFactory.getLogger(YandexDictService::class.java)!!
    }
}
