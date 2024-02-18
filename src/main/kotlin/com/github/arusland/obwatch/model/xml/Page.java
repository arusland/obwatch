package com.github.arusland.obwatch.model.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "page")
@XmlAccessorType(XmlAccessType.FIELD)
public class Page {
    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "revision")
    private Revision revision;

    public Page() {}

    public Page(String title, Revision revision) {
        this.title = title;
        this.revision = revision;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Revision getRevision() {
        return revision;
    }

    public void setRevision(Revision revision) {
        this.revision = revision;
    }
}
