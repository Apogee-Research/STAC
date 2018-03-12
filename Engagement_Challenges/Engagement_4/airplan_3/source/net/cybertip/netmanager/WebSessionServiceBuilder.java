package net.cybertip.netmanager;

public class WebSessionServiceBuilder {
    private long sessionExpirationInMinutes;
    private String applicationBaseName;

    public WebSessionServiceBuilder defineSessionExpirationInMinutes(long sessionExpirationInMinutes) {
        this.sessionExpirationInMinutes = sessionExpirationInMinutes;
        return this;
    }

    public WebSessionServiceBuilder defineApplicationBaseName(String applicationBaseName) {
        this.applicationBaseName = applicationBaseName;
        return this;
    }

    public WebSessionService makeWebSessionService() {
        return new WebSessionService(applicationBaseName, sessionExpirationInMinutes);
    }
}