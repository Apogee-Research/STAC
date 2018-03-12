package edu.cyberapex.server;

public class WebSessionServiceBuilder {
    private long sessionExpirationInMinutes;
    private String applicationBaseName;

    public WebSessionServiceBuilder assignSessionExpirationInMinutes(long sessionExpirationInMinutes) {
        this.sessionExpirationInMinutes = sessionExpirationInMinutes;
        return this;
    }

    public WebSessionServiceBuilder fixApplicationBaseName(String applicationBaseName) {
        this.applicationBaseName = applicationBaseName;
        return this;
    }

    public WebSessionService generateWebSessionService() {
        return new WebSessionService(applicationBaseName, sessionExpirationInMinutes);
    }
}