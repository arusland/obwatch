package com.github.arusland.obwatch.model.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;

public class Text {
    private Integer bytes;

    private String content;

    public Text() {}

    public Text(Integer bytes, String content) {
        this.bytes = bytes;
        this.content = content;
    }

    @XmlAttribute(name = "bytes")
    public Integer getBytes() {
        return bytes;
    }

    public void setBytes(Integer bytes) {
        this.bytes = bytes;
    }

    @XmlValue
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
