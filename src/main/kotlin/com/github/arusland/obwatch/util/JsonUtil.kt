package com.github.arusland.obwatch.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory

object JsonUtil {
    private val mapper: ObjectMapper = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    init {
        mapper.registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )
    }

    fun <T> fromJson(json: String, clazz: Class<T>): T {
        try {
            return mapper.readValue(json, clazz)
        } catch (e: Exception) {
            log.error("Failed to parse json: \n{}", json)
            throw e
        }
    }

    fun toJson(obj: Any): String {
        return mapper.writeValueAsString(obj)
    }

    fun toPrettyJson(obj: Any): String {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
    }

    private val log = LoggerFactory.getLogger(JsonUtil::class.java)!!
}
