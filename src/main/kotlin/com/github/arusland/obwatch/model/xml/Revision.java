package com.github.arusland.obwatch.model.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "revision")
@XmlAccessorType(XmlAccessType.FIELD)
public class Revision {
    @XmlElement(name = "format")
    private String format;

    @XmlElement(name = "text")
    private Text text;

    public Revision() {}

    public Revision(String format, Text text) {
        this.format = format;
        this.text = text;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }
}
