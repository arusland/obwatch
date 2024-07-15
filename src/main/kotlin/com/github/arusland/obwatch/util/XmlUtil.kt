package com.github.arusland.obwatch.util

import com.github.arusland.obwatch.model.xml.MediaWiki
import jakarta.xml.bind.JAXBContext
import java.io.StringReader

object XmlUtil {
    private val PAT_CLEAN = Regex(""" xmlns="http://www.mediawiki.org/xml/export-\d+.\d+/" """)

    fun parseXml(xml: String): MediaWiki {
        val jaxbContext = JAXBContext.newInstance(MediaWiki::class.java)
        val unmarshaller = jaxbContext.createUnmarshaller()
        return unmarshaller.unmarshal(StringReader(PAT_CLEAN.replace(xml, " "))) as MediaWiki
    }
}
