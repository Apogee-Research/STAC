package com.virtualpoint.talkers;

import java.net.InetSocketAddress;

public class DialogsConnectionHelper {
    private final DialogsConnection dialogsConnection;

    public DialogsConnectionHelper(DialogsConnection dialogsConnection) {
        this.dialogsConnection = dialogsConnection;
    }

    /**
     * Sends str to the other side of the connection
     *
     * @param str
     * @throws DialogsTrouble
     */
    public void write(String str) throws DialogsTrouble {
        dialogsConnection.write(str.getBytes());
    }

    public boolean isOpen() {
        return dialogsConnection.fetchChannel().isOpen();
    }

    public String obtainRemotePlaceString() {
        InetSocketAddress sa = (InetSocketAddress) dialogsConnection.fetchChannel().remoteAddress();
        return sa.getHostString();
    }

    public int obtainRemotePort() {
        InetSocketAddress sa = (InetSocketAddress) dialogsConnection.fetchChannel().remoteAddress();
        return sa.getPort();
    }
}