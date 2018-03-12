package org.digitaltip.chatroom;

public class AddCustomerCommandBuilder {
    private HangIn withMi;

    public AddCustomerCommandBuilder assignWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public AddCustomerCommand makeAddCustomerCommand() {
        return new AddCustomerCommand(withMi);
    }
}