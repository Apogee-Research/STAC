/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.datamodel;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 *
 * @author user
 */
class MailBox implements Serializable {

    Vector<EmailEvent> mbox;

    public MailBox() {
        mbox = new Vector();

    }

    public void add(EmailEvent e) {
        mbox.add(e);
    }

    public EmailEvent get() {
        EmailEvent e = null;
        try {
            e = mbox.firstElement();
            if (e != null) {
                mbox.remove(e);
            }
        } catch (NoSuchElementException nse) {
        }
        return e;
    }

}
