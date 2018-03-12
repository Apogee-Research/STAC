package com.networkapex.nethost;

public class WebTemplateBuilder {
    private Class<?> loader;
    private String resourceName;

    public WebTemplateBuilder defineLoader(Class<?> loader) {
        this.loader = loader;
        return this;
    }

    public WebTemplateBuilder defineResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    public WebTemplate generateWebTemplate() {
        return new WebTemplate(resourceName, loader);
    }
}