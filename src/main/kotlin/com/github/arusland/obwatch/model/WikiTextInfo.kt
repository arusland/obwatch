package com.github.arusland.obwatch.model

open class WikiTextInfo(
    val word: String,
    val type: String,
    val examples: List<String>,
    val meanings: Int,
    val baseForm: String,
    val next: WikiTextInfo?
) {
    open fun isEmpty(): Boolean = this.javaClass === WikiTextInfo::class.java && examples.isEmpty()

    open fun isNotEmpty(): Boolean = !isEmpty()

    open fun hasTable(): Boolean = false

    override fun toString(): String {
        return "WikiTextInfo(word='$word', type='$type', examples='$examples')"
    }
}

class VerbInfo(
    word: String,
    type: String,
    examples: List<String>,
    meanings: Int,
    baseForm: String,
    next: WikiTextInfo?,
    val praeterium: String,
    val partizip2: String,
    val hilfsVerb: String
) : WikiTextInfo(word, type, examples, meanings, baseForm, next) {

    override fun hasTable(): Boolean = true

    override fun toString(): String {
        return "VerbInfo(word='$word', type='$type', examples='$examples', praeterium='$praeterium', partizip2='$partizip2', hilfsVerb='$hilfsVerb')"
    }
}

class NounInfo(
    word: String,
    type: String,
    examples: List<String>,
    meanings: Int,
    baseForm: String,
    next: WikiTextInfo?,
    val genus: Genus,
    val cases: List<CaseInfo>
) : WikiTextInfo(word, type, examples, meanings, baseForm, next) {

    override fun toString(): String {
        return "NounInfo(word='$word', type='$type', examples='$examples', genus='$genus', cases=$cases)"
    }

    override fun hasTable() = genus != Genus.NONE

    override fun isEmpty(): Boolean {
        return !hasTable() && examples.isEmpty()
    }
}

class AdjectiveInfo(
    word: String,
    type: String,
    examples: List<String>,
    meanings: Int,
    baseForm: String,
    next: WikiTextInfo?,
    val komparativ: String,
    val superlativ: String,
) : WikiTextInfo(word, type, examples, meanings, baseForm, next) {

    override fun hasTable(): Boolean = komparativ.isNotEmpty() || superlativ.isNotEmpty()

    override fun toString(): String {
        return "AdjectiveInfo(word='$word', type='$type', examples='$examples', komparativ='$komparativ', superlativ='$superlativ')"
    }
}

class CaseInfo(val type: CaseType, val genus: Genus, val singular: String, val plural: String) {
    val singularFull: String
        get() = if (hasSingular()) singularArticle() + " " + singular else NO

    val pluralFull: String
        get() = if (hasPlural()) pluralArticle() + " " + plural else NO


    fun hasSingular(): Boolean = singular !in NO_SET

    fun hasPlural(): Boolean = plural !in NO_SET

    fun singularArticle(): String = when (genus) {
        Genus.MASCULINUM ->
            when (type) {
                CaseType.NOMINATIV -> "der"
                CaseType.GENITIV -> "des"
                CaseType.DATIV -> "dem"
                CaseType.AKKUSATIV -> "den"
            }

        Genus.FEMININUM ->
            when (type) {
                CaseType.NOMINATIV -> "die"
                CaseType.GENITIV -> "der"
                CaseType.DATIV -> "der"
                CaseType.AKKUSATIV -> "die"
            }

        Genus.NEUTRUM ->
            when (type) {
                CaseType.NOMINATIV -> "das"
                CaseType.GENITIV -> "des"
                CaseType.DATIV -> "dem"
                CaseType.AKKUSATIV -> "das"
            }

        Genus.NONE, Genus.FEMMAS -> ""
    }

    fun pluralArticle(): String = when (type) {
        CaseType.NOMINATIV -> "die"
        CaseType.GENITIV -> "der"
        CaseType.DATIV -> "den"
        CaseType.AKKUSATIV -> "die"
    }

    override fun toString(): String {
        return "CaseInfo(type=$type, genus=${genus.value}, singular='$singularFull', plural='$pluralFull')"
    }

    private companion object {
        const val NO = "-"
        val NO_SET = mutableSetOf("—", "-")
    }
}

enum class Genus(val value: String) {
    MASCULINUM("m"),

    FEMININUM("f"),

    NEUTRUM("n"),

    FEMMAS("mf"),

    NONE("");

    companion object {
        fun fromValue(value: String): Genus {
            return when (value) {
                "m" -> MASCULINUM
                "f" -> FEMININUM
                "n" -> NEUTRUM
                "fm" -> FEMMAS
                "", "0" -> NONE
                else -> throw IllegalArgumentException("Unknown value: '$value'")
            }
        }
    }
}

enum class CaseType(val value: String) {
    NOMINATIV("Nominativ"),

    GENITIV("Genitiv"),

    DATIV("Dativ"),

    AKKUSATIV("Akkusativ");
}
