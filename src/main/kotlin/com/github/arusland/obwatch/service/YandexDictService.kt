package com.github.arusland.obwatch.service

import com.github.arusland.obwatch.model.DictResult
import com.github.arusland.obwatch.util.JsonUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory

class YandexDictService(private val apiKey: String) : DictService {
    private val client = OkHttpClient()

    override fun lookup(term: String, lang: DictLang): DictResult {
        val term = term.trim()
        val url = "https://dictionary.yandex.net/api/v1/dicservice.json/lookup?key=$apiKey&lang=${lang.def}&text=$term"

        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            log.error("Failed to lookup word: {}, resp: {}", term, response)
            return DictResult(emptyList())
        }

        val json = response.body!!.string()
        return JsonUtil.fromJson(json, DictResult::class.java)
    }

    private companion object {
        private val log = LoggerFactory.getLogger(YandexDictService::class.java)!!
    }
}

enum class DictLang(val def: String) {
    EN_DE("en-de"),
    DE_RU("de-ru"),
    RU_DE("ru-de"),
    RU_EN("ru-en");
}
