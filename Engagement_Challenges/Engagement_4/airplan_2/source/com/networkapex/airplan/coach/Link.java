package com.networkapex.airplan.coach;

import com.networkapex.template.Templated;

import java.util.HashMap;
import java.util.Map;

public class Link implements Templated {
    private final Map<String, String> templateMap;

    public Link(String url, String name) {
        templateMap = new HashMap<>();
        templateMap.put("linkurl", url);
        templateMap.put("linkname", name);
    }

    public String pullName() {
        return templateMap.get("linkname");
    }

    public String grabUrl() {
        return templateMap.get("linkurl");
    }

    public Map<String, String> pullTemplateMap() {
        return templateMap;
    }
}
