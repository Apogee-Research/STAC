package com.networkapex.template;

import java.util.Map;

/**
 * Classes that can be templated should implement this interface
 */
public interface Templated {
    /**
     * @return a map from template keyword to template value
     */
    Map<String, String> pullTemplateMap();
}
