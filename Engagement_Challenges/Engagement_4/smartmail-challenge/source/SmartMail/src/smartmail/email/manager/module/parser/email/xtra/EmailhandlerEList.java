/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.email.manager.module.parser.email.xtra;

import java.util.List;
import smartmail.datamodel.EmailAddress;
import smartmail.datamodel.EmailEvent;
import java.util.Set;

/**
 *
 * @author burkep
 */
public class EmailhandlerEList {

    public String lastPacket;
    EmailEvent lastEvent;
    int lastEventSt = 1;

    public int lastEventStatus() {
        int ret = lastEventSt;
        lastEventSt = 1;
        return ret;
    }

    public EmailEvent lastEvent() {
        EmailEvent ret = lastEvent;
        lastEvent = null;
        return ret;
    }

    @Override
    public String toString() {

        String ret = lastPacket;
        lastPacket = null;
        return ret;
    }

    void setAddrs(String prot, String src, String dest, EmailEvent evt) {
        smartmail.datamodel.EmailAddress srcaddr = new smartmail.datamodel.EmailAddress();
        srcaddr.setProtocol(prot);
        srcaddr.setValue(src);
        Set<EmailAddress> othersource = evt.getOthersource();
        othersource.add(srcaddr);
        smartmail.datamodel.EmailAddress destaddr = new smartmail.datamodel.EmailAddress();
        destaddr.setProtocol(prot);
        destaddr.setValue(dest);
        List<EmailAddress> otherdest = evt.getOtherdestination();
        otherdest.add(destaddr);
    }

};
