package com.cyberpointllc.stac.gabfeed.handler;

import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.template.Templated;
import java.util.Map;

public class Link implements Templated {

    private final Map<String, String> templateMap;

    public Link(String url, String name) {
        templateMap = new  HashMap();
        templateMap.put("linkurl", url);
        templateMap.put("linkname", name);
    }

    public String getName() {
        return templateMap.get("linkname");
    }

    public String getUrl() {
        return templateMap.get("linkurl");
    }

    public Map<String, String> getTemplateMap() {
        return templateMap;
    }
}
