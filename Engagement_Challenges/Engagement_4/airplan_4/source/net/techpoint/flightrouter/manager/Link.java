package net.techpoint.flightrouter.manager;

import net.techpoint.template.Templated;

import java.util.HashMap;
import java.util.Map;

public class Link implements Templated {
    private final Map<String, String> templateMap;

    public Link(String url, String name) {
        templateMap = new HashMap<>();
        templateMap.put("linkurl", url);
        templateMap.put("linkname", name);
    }

    public String fetchName() {
        return templateMap.get("linkname");
    }

    public String getUrl() {
        return templateMap.get("linkurl");
    }

    public Map<String, String> takeTemplateMap() {
        return templateMap;
    }
}
