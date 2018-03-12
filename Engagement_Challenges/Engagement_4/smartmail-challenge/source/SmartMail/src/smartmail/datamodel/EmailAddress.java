/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.datamodel;

import smartmail.email.manager.module.addressbook.AddressBook;
import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Pattern;
import org.apache.james.mime4j.dom.address.Address;

/**
 *
 * @author user
 */
public class EmailAddress extends MessageWord implements Comparable<EmailAddress> {

    private Integer id;

    private String protocol;
    static AddressBook abook;
    int lastuniqueaddressid;

    MailBox mbox;

    public static EmailAddress checkAddress(EmailAddress a) throws IOException {
        abook = AddressBook.getAddressBook();

        //STAC: Get the address out of the AddressBook
        EmailAddress lookupAddress = abook.lookupAddress(a.getValue());
        return lookupAddress;
    }

    public EmailAddress(int lastuniqueaddressid) {
        this.lastuniqueaddressid = lastuniqueaddressid;

        mbox = new MailBox();
    }

    public EmailAddress() {
        this.lastuniqueaddressid = 0;
    }

    public String getUniqueValue() {

        return this.getValue() + ":" + lastuniqueaddressid;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Object getPK() {
        return id;
    }

    @Override
    public int compareTo(EmailAddress a) {
        EmailAddress ae = (EmailAddress) a;
        int v = getUniqueValue().compareTo(ae.getUniqueValue());
        return v;
    }

    public boolean addToMailBox(EmailEvent e) {
        if (mbox != null) {
            mbox.add(e);
            return true;
        }
        return false;
    }

    public EmailEvent getFromMailBox() {
        if (mbox != null) {
            EmailEvent e = mbox.get();
            return e;
        }
        return null;
    }

    public void setValue(String value) {
        //System.out.println("v:" + value);
        value = value.toLowerCase();
        int indexOfAt = value.indexOf('@');
        String mbox = value.substring(0, indexOfAt);

        boolean hasNonAlpha = mbox.matches("^.*[^a-z].*$");
        //(value);
        if (!hasNonAlpha) {
            super.setValue(value);
        }
    }

}
