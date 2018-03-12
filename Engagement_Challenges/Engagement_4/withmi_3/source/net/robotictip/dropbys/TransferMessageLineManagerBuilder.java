package net.robotictip.dropbys;

public class TransferMessageLineManagerBuilder {
    private HangIn withMi;

    public TransferMessageLineManagerBuilder setWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public TransferMessageLineManager generateTransferMessageLineManager() {
        return new TransferMessageLineManager(withMi);
    }
}