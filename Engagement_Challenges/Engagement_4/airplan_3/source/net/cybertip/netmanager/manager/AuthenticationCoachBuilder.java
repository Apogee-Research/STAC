package net.cybertip.netmanager.manager;

public class AuthenticationCoachBuilder {
    private String redirectResponsePath;

    public AuthenticationCoachBuilder assignRedirectResponsePath(String redirectResponsePath) {
        this.redirectResponsePath = redirectResponsePath;
        return this;
    }

    public AuthenticationCoach makeAuthenticationCoach() {
        return new AuthenticationCoach(redirectResponsePath);
    }
}