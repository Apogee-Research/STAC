package com.cyberpointllc.stac.webserver;

import com.cyberpointllc.stac.hashmap.HashMap;
import java.util.Map;

/**
 * Helps keep track of the current user.s
 */
public class WebSession {

    private final String userId;

    private final Map<String, String> propertyMap = new  HashMap();

    public WebSession(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public String getProperty(String name) {
        return propertyMap.get(name);
    }

    public String getProperty(String name, String defaultReturn) {
        if (propertyMap.containsKey(name)) {
            return propertyMap.get(name);
        } else {
            return defaultReturn;
        }
    }

    public void setProperty(String name, String value) {
        ClasssetProperty replacementClass = new  ClasssetProperty(name, value);
        ;
        replacementClass.doIt0();
    }

    public class ClasssetProperty {

        public ClasssetProperty(String name, String value) {
            this.name = name;
            this.value = value;
        }

        private String name;

        private String value;

        public void doIt0() {
            propertyMap.put(name, value);
        }
    }
}
