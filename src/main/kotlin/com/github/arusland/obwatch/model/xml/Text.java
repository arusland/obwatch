package com.github.arusland.obwatch.model.xml;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "text")
@XmlAccessorType(XmlAccessType.FIELD)
public class Text {
    @XmlAttribute(name = "bytes")
    private Integer bytes;

    @XmlValue
    private String content;

    public Text() {}

    public Text(Integer bytes, String content) {
        this.bytes = bytes;
        this.content = content;
    }

    public Integer getBytes() {
        return bytes;
    }

    public void setBytes(Integer bytes) {
        this.bytes = bytes;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
