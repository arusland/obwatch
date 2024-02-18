package com.github.arusland.obwatch.model.xml;

import jakarta.xml.bind.annotation.XmlElement;

public class Revision {
    private String format;

    private Text text;

    public Revision() {}

    public Revision(String format, Text text) {
        this.format = format;
        this.text = text;
    }

    @XmlElement(name = "format")
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @XmlElement(name = "text")
    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }
}
