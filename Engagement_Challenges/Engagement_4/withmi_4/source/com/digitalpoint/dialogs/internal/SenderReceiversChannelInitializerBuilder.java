package com.digitalpoint.dialogs.internal;

import com.digitalpoint.dialogs.SenderReceiversCoach;
import com.digitalpoint.dialogs.SenderReceiversIdentity;

public class SenderReceiversChannelInitializerBuilder {
    private SenderReceiversIdentity identity;
    private SenderReceiversCoach coach;
    private boolean isServer;

    public SenderReceiversChannelInitializerBuilder assignIdentity(SenderReceiversIdentity identity) {
        this.identity = identity;
        return this;
    }

    public SenderReceiversChannelInitializerBuilder fixCoach(SenderReceiversCoach coach) {
        this.coach = coach;
        return this;
    }

    public SenderReceiversChannelInitializerBuilder fixIsServer(boolean isServer) {
        this.isServer = isServer;
        return this;
    }

    public SenderReceiversChannelInitializer makeSenderReceiversChannelInitializer() {
        return new SenderReceiversChannelInitializer(coach, identity, isServer);
    }
}