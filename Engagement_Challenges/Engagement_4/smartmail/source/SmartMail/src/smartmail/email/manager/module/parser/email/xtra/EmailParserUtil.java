/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.email.manager.module.parser.email.xtra;

/**
 *
 * @author burkep
 */
import smartmail.datamodel.EmailEvent;
import smartmail.process.controller.module.seqfile.EmailParseException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

//TODO: TO eMAILpARSER
public class EmailParserUtil {

    private EmailDirHandler email;

    public static List<EmailEvent> parseemails(File filen) throws EmailParseException {
        List<EmailEvent> ces = new ArrayList<EmailEvent>();
        System.out.println("starting parser");
        EmailParserUtil emaill = new EmailParserUtil();

        emaill.init(filen);

        EmailEvent emailevent = null;
        int eventorder = 0;
        do {
            //STAC: Cycle through the emails -- should only ever be one, but maybe a blue team wll get confused
            //STAC: and think there is a way to add more and mistake this as a vulnerability
            emailevent = emaill.getNextEmailEvent(1, ces, eventorder);
            eventorder++;
        } while (emailevent != null);
        System.out.println("done parsing");
        emaill.close();
        return ces;
    }

    public EmailEvent getNextEmailEvent(int numloops, List<EmailEvent> ces, int eventorder) {
        final StringBuilder errbuf = new StringBuilder();

        EmailEvent emailevent = new EmailEvent();
        if (email.getNext(emailevent, errbuf)) {
            ces.add(emailevent);

            return emailevent;
        }

        return null;
    }

    public void init(final File file) throws EmailParseException {

        final StringBuilder errbuf = new StringBuilder();
        email = new EmailDirHandler();
        email.openOffline(file.getAbsolutePath(), errbuf);

    }

    private void close() {
        email.close();
    }

}
