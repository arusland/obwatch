package com.github.arusland.obwatch.util

import com.github.arusland.obwatch.model.xml.MediaWiki
import jakarta.xml.bind.JAXBContext
import java.io.StringReader

object XmlUtil {

    fun parseXml(xml: String): MediaWiki {
        val jaxbContext = JAXBContext.newInstance(MediaWiki::class.java)
        val unmarshaller = jaxbContext.createUnmarshaller()
        return unmarshaller.unmarshal(StringReader(xml.replace(" xmlns=\"http://www.mediawiki.org/xml/export-0.10/\" ", " "))) as MediaWiki
    }
}
