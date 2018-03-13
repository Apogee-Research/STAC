/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.logging.module;

import com.google.common.collect.Multimap;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.io.Text;
import smartmail.datamodel.EmailAddress;
import smartmail.datamodel.EmailEvent;
import smartmail.email.manager.module.addressbook.AddressBook;
import smartmail.logging.module.crypto.CryptoException;
import smartmail.logging.module.crypto.CryptoUtils;
import smartmail.process.controller.module.PipelineController;

/**
 *
 * @author user
 */
public class LogGenerator {

    public static void checkValuesandOutput(Multimap output, List<EmailEvent> es) {
        TreeSet keys = new TreeSet(output.keySet());
        Iterator keyit = keys.iterator();

        //STAC: Loop over aggregated results form the Reducer, create a log for each and output
        while (keyit.hasNext()) {

            try {
                Text next = (Text) keyit.next();

                Collection get = output.get(next);

                Iterator iterator = get.iterator();
                String redval = (String) iterator.next();

                String[] split = redval.split(":");
                int cnt = Integer.valueOf(split[0]);
                boolean sec = Boolean.valueOf(split[1]);
                //STAC: Create a log message that includes the email address and the cnt, separated by a ':'
                String logm = next.toString() + ":" + cnt;
                if (logm.length() < 100) {
                    int diff = 100 - logm.length();
                    SecureRandom random = new SecureRandom();
                    String pad = new BigInteger(diff, random).toString(32);
                    //STAC: Pad our log message
                    logm = logm + ":" + pad;
                }

                Iterator<EmailEvent> it = es.iterator();

                //STAC: See if address has an associated Mailbox
                EmailAddress lookupExistingAddress = AddressBook.getAddressBook().lookupExistingAddress(next.toString());

                LogWriter.writelogandplaceinmailbox(logm, lookupExistingAddress, it, cnt, sec);
            } catch (IOException ex) {
                Logger.getLogger(LogGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
}
