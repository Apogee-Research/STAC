package edu.cyberapex.server;

public class WebSessionBuilder {
    private String memberId;

    public WebSessionBuilder fixMemberId(String memberId) {
        this.memberId = memberId;
        return this;
    }

    public WebSession generateWebSession() {
        return new WebSession(memberId);
    }
}