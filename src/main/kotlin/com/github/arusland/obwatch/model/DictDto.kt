package com.github.arusland.obwatch.model

data class DictResult(
    val def: List<Definition>
)

data class Definition(
    val text: String,
    val pos: String,
    val ts: String,
    val tr: List<Translation>
)

data class Translation(
    val text: String,
    val pos: String,
    val fr: Int,
    val gen: String?,
    val syn: List<Synonym>?,
    val mean: List<Meaning>?
)

data class Synonym(
    val text: String,
    val pos: String,
    val fr: Int
)

data class Meaning(
    val text: String
)
