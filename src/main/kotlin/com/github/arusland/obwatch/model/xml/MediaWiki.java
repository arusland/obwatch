package com.github.arusland.obwatch.model.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "mediawiki")
@XmlAccessorType(XmlAccessType.FIELD)
public class MediaWiki {

    @XmlElement(name = "page")
    private Page page;

    public MediaWiki() {
    }

    public MediaWiki(Page page) {
        this.page = page;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }
}
