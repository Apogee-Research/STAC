package com.networkapex.nethost.coach;

public class AuthenticationManagerBuilder {
    private String redirectResponseTrail;

    public AuthenticationManagerBuilder defineRedirectResponseTrail(String redirectResponseTrail) {
        this.redirectResponseTrail = redirectResponseTrail;
        return this;
    }

    public AuthenticationManager generateAuthenticationManager() {
        return new AuthenticationManager(redirectResponseTrail);
    }
}