package net.cybertip.netmanager;

public class WebSessionBuilder {
    private String memberId;

    public WebSessionBuilder assignMemberId(String memberId) {
        this.memberId = memberId;
        return this;
    }

    public WebSession makeWebSession() {
        return new WebSession(memberId);
    }
}