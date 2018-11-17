package com.infinite.minio.entity;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Album")
public class Album {
    private String url;
    private String description;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
