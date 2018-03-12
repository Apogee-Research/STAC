package com.roboticcusp.network.coach;

public class AuthenticationCoachBuilder {
    private String redirectResponseTrail;

    public AuthenticationCoachBuilder fixRedirectResponseTrail(String redirectResponseTrail) {
        this.redirectResponseTrail = redirectResponseTrail;
        return this;
    }

    public AuthenticationCoach composeAuthenticationCoach() {
        return new AuthenticationCoach(redirectResponseTrail);
    }
}