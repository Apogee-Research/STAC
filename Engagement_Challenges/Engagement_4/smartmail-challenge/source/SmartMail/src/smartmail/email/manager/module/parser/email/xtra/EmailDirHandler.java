/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.email.manager.module.parser.email.xtra;

import smartmail.datamodel.EmailEvent;
import smartmail.email.manager.module.parser.MailContentHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.MimeConfig;

/**
 *
 * @author user
 */
class EmailDirHandler {

    private File emaildir;

    File[] listFiles;

    int index = 0;

    void close() {

    }

    void openOffline(String absolutePath, StringBuilder errbuf) {

        emaildir = new File(absolutePath);

        listFiles = emaildir.listFiles();

    }

    boolean getNext(EmailEvent emailevent, StringBuilder errbuf) {

        boolean foundf = false;

        while (!foundf && index < listFiles.length) {

            if (listFiles[index].getName().endsWith(".mime")) {
                System.out.println("opening email:" + listFiles[index].getName());

                //STAC: Set up and Call the Mime Email parser
                //STAC: MailContentHandler is a callback that implements the email handling
                ContentHandler handler = new MailContentHandler(true, emailevent);
                MimeConfig config = new MimeConfig();
                MimeStreamParser parser = new MimeStreamParser(config);
                parser.setContentHandler(handler);
                InputStream instream = null;
                try {
                    System.out.println("opening email:" + listFiles[index]);
                    instream = new FileInputStream(listFiles[index]);

                    parser.parse(instream);
                } catch (MimeException ex) {
                    Logger.getLogger(EmailDirHandler.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                } catch (IOException ex) {
                    Logger.getLogger(EmailDirHandler.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                } finally {
                    try {
                        instream.close();
                    } catch (IOException ex) {
                        Logger.getLogger(EmailDirHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                //STAC: Forces app to only process one file, but there is never more than one anyway, kinda looks like a bug
                foundf = true;

            }
            index++;

        }

        return foundf;
    }

}
