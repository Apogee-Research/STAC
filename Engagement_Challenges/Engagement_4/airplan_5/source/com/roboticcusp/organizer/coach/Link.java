package com.roboticcusp.organizer.coach;

import com.roboticcusp.template.Templated;

import java.util.HashMap;
import java.util.Map;

public class Link implements Templated {
    private final Map<String, String> templateMap;

    public Link(String url, String name) {
        templateMap = new HashMap<>();
        templateMap.put("linkurl", url);
        templateMap.put("linkname", name);
    }

    public String getName() {
        return templateMap.get("linkname");
    }

    public String pullUrl() {
        return templateMap.get("linkurl");
    }

    public Map<String, String> obtainTemplateMap() {
        return templateMap;
    }
}
