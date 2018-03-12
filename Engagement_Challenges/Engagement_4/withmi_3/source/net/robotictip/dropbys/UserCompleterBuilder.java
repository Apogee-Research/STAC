package net.robotictip.dropbys;

public class UserCompleterBuilder {
    private HangIn withMi;

    public UserCompleterBuilder setWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public UserCompleter generateUserCompleter() {
        return new UserCompleter(withMi);
    }
}