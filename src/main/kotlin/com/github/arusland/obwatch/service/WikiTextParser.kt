package com.github.arusland.obwatch.service

import org.eclipse.mylyn.wikitext.parser.DocumentBuilder
import org.eclipse.mylyn.wikitext.parser.MarkupParser
import org.eclipse.mylyn.wikitext.parser.builder.HtmlDocumentBuilder
import org.eclipse.mylyn.wikitext.parser.markup.MarkupLanguage
import org.eclipse.mylyn.wikitext.util.ServiceLocator
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets


class WikiTextParser {
    fun transformToHtml(markupLanguageName: String, input: String, output: OutputStream) {
        val markupLanguage = ServiceLocator.getInstance().getMarkupLanguage(markupLanguageName)
        transformToHtml(markupLanguage, input, output)
    }

    fun transformToHtml(markupLanguage: MarkupLanguage, input: String, output: OutputStream) {
        val parser = MarkupParser(markupLanguage)
        val builder: DocumentBuilder = createDocumentBuilder(output)
        parser.builder = builder
        parser.parse(input)
    }

    private fun createDocumentBuilder(output: OutputStream): DocumentBuilder {
        return HtmlDocumentBuilder(OutputStreamWriter(output, StandardCharsets.UTF_8), true)
    }
}

fun main() {

}
