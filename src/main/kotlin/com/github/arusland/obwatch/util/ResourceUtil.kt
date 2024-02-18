package com.github.arusland.obwatch.util

import java.io.IOException

object ResourceUtil {
    fun readResource(resourceName: String): String {
        ResourceUtil::class.java.getResourceAsStream(resourceName).use { stream ->
            return stream?.reader()?.readText() ?: throw IOException("Resource not found: $resourceName")
        }
    }
}
