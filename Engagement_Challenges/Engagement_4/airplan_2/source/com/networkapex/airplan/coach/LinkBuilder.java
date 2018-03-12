package com.networkapex.airplan.coach;

public class LinkBuilder {
    private String name;
    private String url;

    public LinkBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public LinkBuilder assignUrl(String url) {
        this.url = url;
        return this;
    }

    public Link generateLink() {
        return new Link(url, name);
    }
}