package com.github.arusland.obwatch.model

open class WikiTextInfo(val word: String, val type: String, val examples: List<String>)

class VerbInfo(
    word: String,
    type: String,
    examples: List<String>,
    val praeterium: String,
    val partizip2: String,
    val hilfsVerb: String
) :
    WikiTextInfo(word, type, examples) {

    override fun toString(): String {
        return "VerbInfo(word='$word', type='$type', examples='$examples', praeterium='$praeterium', partizip2='$partizip2', hilfsVerb='$hilfsVerb')"
    }
}

class NounInfo(
    word: String,
    type: String,
    examples: List<String>,
    val genus: String,
    val cases: Map<CaseType, CaseInfo>
) :
    WikiTextInfo(word, type, examples) {

    override fun toString(): String {
        return "NounInfo(word='$word', type='$type', examples='$examples', genus='$genus', cases=$cases)"
    }
}

class CaseInfo(val singular: String, val plural: String) {
    override fun toString(): String {
        return "CaseInfo(singular='$singular', plural='$plural')"
    }
}

enum class CaseType(val value: String) {
    NOMINATIV("Nominativ"),

    GENITIV("Genitiv"),

    DATIV("Dativ"),

    AKKUSATIV("Akkusativ");
}
