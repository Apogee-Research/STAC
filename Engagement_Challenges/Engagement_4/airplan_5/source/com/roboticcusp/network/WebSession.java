package com.roboticcusp.network;

import java.util.HashMap;
import java.util.Map;

/**
 * Helps keep track of the current user.s
 */
public class WebSession {
    private final String participantId;
    private final Map<String, String> propertyMap = new HashMap<>();

    public WebSession(String participantId) {
        this.participantId = participantId;
    }

    public String grabParticipantId() {
        return participantId;
    }

    public String obtainProperty(String name) {
        return propertyMap.get(name);
    }

    public String pullProperty(String name, String defaultReturn) {
        if (propertyMap.containsKey(name)) {
            return propertyMap.get(name);
        } else {
            return defaultReturn;
        }
    }

    public void setProperty(String name, String value) {
        propertyMap.put(name, value);
    }
}
