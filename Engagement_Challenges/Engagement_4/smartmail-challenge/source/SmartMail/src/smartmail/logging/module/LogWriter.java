/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.logging.module;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
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
class LogWriter {

    public static void writelogandplaceinmailbox(String logm, EmailAddress lookupExistingAddress, Iterator<EmailEvent> it, int cnt, boolean sec) {
        try {
            //STAC: COMMENT: PLAN TO MAKE DECRYPTING OFF LIMITS, SEND US THOUGHTS IF YOU DISAGREE!
            String strencrypt = CryptoUtils.strencrypt("thekeyisbigenoug", logm);

            //STAC: Write out the encrypted log
            CryptoUtils.write(strencrypt, cnt, sec);

            while (it.hasNext()) {
                EmailEvent next1 = it.next();
                //STAC: If the Email address exists in Addressbook, put it in in-memory mailbox, or else do nothing                 
                if (lookupExistingAddress != null) {

                    lookupExistingAddress.addToMailBox(next1);
                }
            }

        } catch (CryptoException ex) {
            Logger.getLogger(PipelineController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
