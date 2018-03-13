/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.datamodel;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author user
 */
public class MailingList extends EmailAddress {

    private Set<EmailAddress> members;

    public MailingList(int uid) {
        super(uid);
        members = new TreeSet();
    }

    public Set<EmailAddress> getMembers() {
        return members;
    }

    public void setMember(Set<EmailAddress> members) {
        this.members = members;
    }

    public void addMember(EmailAddress member) {

        this.members.add(member);

    }

    public Set<EmailAddress> getPublicMembers() throws IOException {

        Set<EmailAddress> pmembers = new TreeSet();
        Iterator<EmailAddress> it = members.iterator();

        while (it.hasNext()) {
            EmailAddress next = it.next();
            boolean secretAddress = SecureEmailAddress.isSecretAddress(next);
            if (!secretAddress) {
                pmembers.add(next);
            }
        }

        return pmembers;
    }

}
