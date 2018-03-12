package smartmail.email.manager.module.addressbook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import smartmail.datamodel.EmailAddress;
import smartmail.datamodel.MailingList;
import smartmail.datamodel.SecureEmailAddress;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author user
 */
public class AddressBook {

    /*static String[] securedaddresses = {"andy@smartmail.com:andrew@smartmail.com", "frank@smartmail.com", "jack@smartmail.com",
     "linda@smartmail.com", "nick@smartmail.com", "zelda@smartmail.com"};
     static String[] mlists = {"security@smartmail.com:andrew@smartmail.com:frank@smartmail.com:jack@smartmail.com:linda@smartmail.com:nick@smartmail.com:zelda@smartmail.com",
     "minsecurity@smartmail.com:andrew@smartmail.com:frank@smartmail.com:linda@smartmail.com:nick@smartmail.com:zelda@smartmail.com"
     };*/
    static String[] securedaddresses;
    static String[] mlists;

    Map<String, EmailAddress> abook;
    Map<Integer, EmailAddress> abookbyID;

    public static int lastuniqueaddressid = 0;

    static AddressBook theabook = null;

    public static AddressBook getAddressBook() throws IOException {

        if (theabook == null) {
            securedaddresses = loadList("addresses");
            mlists = loadList("mlists");

            theabook = new AddressBook();
        }
        return theabook;
    }

    private AddressBook() throws IOException {

        abook = new HashMap<String, EmailAddress>();
        abookbyID = new HashMap<Integer, EmailAddress>();
        populateAddressBook();

    }

    public EmailAddress lookupExistingAddress(String val) {
        EmailAddress address = abook.get(val);

        if (address == null) {
            System.out.println("existing address null");

        }

        return address;
    }

    public EmailAddress lookupAddress(String val) {
        EmailAddress address = abook.get(val);

        if (address == null) {
            //STAC: if the address is not in AddressBook, make a temp one. 
            //STAC: Does not add in AddresBook, only exists in memory for this request; it's then forgotten
            System.out.println("null");
            address = new EmailAddress();
            address.setValue(val);
        }

        return address;
    }

    boolean isSecretAddress(EmailAddress a) throws IOException {

        SecureEmailAddress sea = new SecureEmailAddress(lastuniqueaddressid);

        return sea.isSecretAddress(a);
    }

    public void populateAddressBook() throws IOException {

        for (int i = 0; i < securedaddresses.length; i++) {
            int indexOf = securedaddresses[i].indexOf(':');
            if (indexOf > 0) {
                String eadd1 = securedaddresses[i].substring(0, indexOf);
                String eadd2 = securedaddresses[i].substring(indexOf + 1, securedaddresses[i].length());
                EmailAddress emailAddress1 = makeEmail(lastuniqueaddressid, eadd1);

                EmailAddress emailAddress2 = makeEmail(lastuniqueaddressid, eadd2);

                abook.put(eadd1, emailAddress1);
                abook.put(eadd2, emailAddress2);
                abookbyID.put(lastuniqueaddressid, emailAddress1);
            } else {
                EmailAddress emailAddress1 = makeEmail(lastuniqueaddressid, securedaddresses[i]);//ew EmailAddress(lastuniqueaddressid);
                //emailAddress1.setValue(securedaddresses[i]);
                abook.put(securedaddresses[i], emailAddress1);
                abookbyID.put(lastuniqueaddressid, emailAddress1);
            }

            lastuniqueaddressid++;
        }

        for (int i = 0; i < mlists.length; i++) {
            String[] split = mlists[i].split(":");

            MailingList mailingList = new MailingList(lastuniqueaddressid);
            mailingList.setValue(split[0]);

            for (int j = 1; j < split.length; j++) {
                EmailAddress existingAddress = lookupExistingAddress(split[j]);
                mailingList.addMember(existingAddress);

            }
            abook.put(split[0], mailingList);
            abookbyID.put(lastuniqueaddressid, mailingList);
            lastuniqueaddressid++;

        }
    }

    public Set<EmailAddress> getAddresses(String pword) {

        Set<String> keySet = abook.keySet();

        Iterator<String> it = keySet.iterator();

        while (it.hasNext()) {

            it.next();
        }
        //TODO
        return null;
    }

    private EmailAddress makeEmail(int lastuniqueaddressid, String eadd1) throws IOException {

        EmailAddress email = null;

        if (SecureEmailAddress.isSecretAddress(eadd1)) {
            email = new SecureEmailAddress(lastuniqueaddressid);
        } else {
            email = new EmailAddress(lastuniqueaddressid);
        }
        email.setValue(eadd1);

        return email;

    }

    public static String[] loadList(String fname) throws IOException {
        List<String> list = new ArrayList();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("data/" + fname);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            for (String line; (line = br.readLine()) != null;) {

                list.add(line);

            }
        }
        String[] lista = new String[list.size()];
        list.toArray(lista);
        return lista;
    }

}
