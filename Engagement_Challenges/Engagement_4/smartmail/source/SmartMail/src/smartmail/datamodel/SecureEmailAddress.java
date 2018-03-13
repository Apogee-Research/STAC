/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.datamodel;

import static smartmail.email.manager.module.addressbook.AddressBook.loadList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author user
 */
public class SecureEmailAddress extends EmailAddress {

    static String[] securedaddresses;// = {"jack@smartmail.com"};
    static List<String> securedl;

    //EmailAddress actual;
    public SecureEmailAddress(int lastuniqueaddressid) throws IOException {

        super(lastuniqueaddressid);
        if (securedl == null) {
            securedaddresses = loadList("securedaddresses");
            securedl = new ArrayList<String>();
            for (int i = 0; i < securedaddresses.length; i++) {
                securedl.add(securedaddresses[i]);
            }
        }
    }

    public static boolean isSecretAddress(EmailAddress a) throws IOException {

        if (securedl == null) {
            securedaddresses = loadList("securedaddresses");
            securedl = new ArrayList<String>();
            for (int i = 0; i < securedaddresses.length; i++) {
                securedl.add(securedaddresses[i]);
            }
        }
        boolean contains = securedl.contains(a.getValue());
        /*if(contains){
        
         this.actual = a;
         return this;
         }*/
        return contains;
    }

    public static boolean isSecretAddress(String add) throws IOException {

        if (securedl == null) {
            securedaddresses = loadList("securedaddresses");
            securedl = new ArrayList<String>();
            for (int i = 0; i < securedaddresses.length; i++) {
                securedl.add(securedaddresses[i]);
            }
        }
        boolean contains = securedl.contains(add);
        /*if(contains){
        
         this.actual = a;
         return this;
         }*/
        return contains;
    }

    /*public String getValue() {
     return actual.getValue();
     }*/
}
