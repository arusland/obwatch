package com.github.arusland.obwatch.service

import com.github.arusland.obwatch.model.DictResult

interface DictService {
    fun lookup(word: String, lang: DictLang): DictResult
}
