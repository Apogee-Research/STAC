package net.computerpoint.dialogs.internal;

import net.computerpoint.dialogs.ProtocolsIdentity;
import net.computerpoint.dialogs.ProtocolsManager;

public class ProtocolsChannelInitializerBuilder {
    private ProtocolsIdentity identity;
    private ProtocolsManager conductor;
    private boolean isServer;

    public ProtocolsChannelInitializerBuilder setIdentity(ProtocolsIdentity identity) {
        this.identity = identity;
        return this;
    }

    public ProtocolsChannelInitializerBuilder assignConductor(ProtocolsManager conductor) {
        this.conductor = conductor;
        return this;
    }

    public ProtocolsChannelInitializerBuilder fixIsServer(boolean isServer) {
        this.isServer = isServer;
        return this;
    }

    public ProtocolsChannelInitializer formProtocolsChannelInitializer() {
        return new ProtocolsChannelInitializer(conductor, identity, isServer);
    }
}