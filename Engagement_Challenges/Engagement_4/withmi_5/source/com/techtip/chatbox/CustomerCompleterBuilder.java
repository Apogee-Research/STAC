package com.techtip.chatbox;

public class CustomerCompleterBuilder {
    private DropBy withMi;

    public CustomerCompleterBuilder assignWithMi(DropBy withMi) {
        this.withMi = withMi;
        return this;
    }

    public CustomerCompleter formCustomerCompleter() {
        return new CustomerCompleter(withMi);
    }
}