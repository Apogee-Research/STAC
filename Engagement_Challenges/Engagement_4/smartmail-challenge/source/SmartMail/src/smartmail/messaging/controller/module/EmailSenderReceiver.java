package smartmail.messaging.controller.module;

import smartmail.process.controller.module.PipelineController;
import smartmail.email.manager.module.addressbook.AddressBook;
import smartmail.datamodel.EmailAddress;
import smartmail.datamodel.EmailEvent;
import smartmail.datamodel.MailingList;
import smartmail.process.controller.module.seqfile.EmailParseException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import static smartmail.messaging.controller.module.RunSmartMail.clean;

public class EmailSenderReceiver extends HttpServlet {

    private final AddressBook abook;

    public EmailSenderReceiver() throws IOException {
        abook = AddressBook.getAddressBook();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //TODO
        StringBuffer requestURL = request.getRequestURL();
        URL url = new URL(requestURL.toString());
        System.out.println("path:" + url.getPath());

        String path = url.getPath();
        switch (path) {
            case "/email.cgi":
                doEmail(request, response);
                break;
            case "/mbox.cgi":
                getMBox(request, response);
                break;
            case "/address.cgi":
                getAddress(request, response);
                break;

            default:
                throw new IllegalArgumentException("ERROR:Unknown request type");
        }
    }

    private void getMBox(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //STAC: Implements request handling functionality for reading from a mailbox
        String mbox = request.getParameter("mbox");
        if (mbox.length() > (25 + "@smartmail.com".length()) || !mbox.endsWith("@smartmail.com")) {
            mbox = mbox.toLowerCase();

            response.getWriter().println("ERROR: Invalid input");
            return;
        }

        //STAC:The AddressBook holds the pointer to the in-memory mailbox for a user
        EmailAddress mboxaddress = AddressBook.getAddressBook().lookupExistingAddress(mbox);

        EmailEvent fromMailBox = mboxaddress.getFromMailBox();

        //Check if any items in MBox
        if (fromMailBox == null) {
            response.getWriter().println("Sorry, Your MailBox is empty.");
        } else {

            //STAC: If there is an item, send it back to User
            EmailAddress source = fromMailBox.getSource();

            response.getWriter().println("From:" + source.getValue());
            response.getWriter().println("Subject:" + fromMailBox.getSubject());
            response.getWriter().println("Message:" + fromMailBox.getContent());
        }

    }

    private void getAddress(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //STAC: Implements request handling functionality for getting public members of a mailing list
        String list = request.getParameter("list");

        System.out.println("list:" + list);

        if (list.length() > (25 + "@smartmail.com".length()) || !list.endsWith("@smartmail.com")) {
            list = list.toLowerCase();
            response.getWriter().println("ERROR: Invalid input");
            return;
        }

        EmailAddress lookupAddress = abook.lookupAddress(list);

        StringBuilder addresses = new StringBuilder();
        if (lookupAddress instanceof MailingList) {
            MailingList ml = (MailingList) lookupAddress;
            Set<EmailAddress> members = ml.getPublicMembers();
            Iterator<EmailAddress> it = members.iterator();
            while (it.hasNext()) {
                addresses.append(it.next().getValue() + ";");
            }

        }
        response.getWriter().println(addresses);

    }

    private void doEmail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //STAC: Implements request handling functionality for send and 'analyzing' an email
        //STAC: Vulnerabilty resides in this request
        String from = request.getParameter("from");
        System.out.println(from);
        from = from.toLowerCase();
        if (from.length() > (25 + "@smartmail.com".length()) || !from.endsWith("@smartmail.com")) {
            response.getWriter().println("ERROR: Invalid input");
            return;
        }
        
        String to = request.getParameter("to");
        
        
        if(to.length()>((25 + "@smartmail.com".length()+1)*10)){
            response.getWriter().println("ERROR: Invalid input, too long:" + to.length());
            return;        
        }
        String[] tos = to.split(";");
        //System.out.println(to + ":" +tos.length);
        //Split the 'To' field addresses
        
        List<String> toList = Arrays.asList(tos);
        List<String> toListLower = new ArrayList<String>();
        for (int i = 0; i < toList.size(); i++) {
            String toitem = toList.get(i);
            if (toitem.length() > (25 + "@smartmail.com".length()) || !toitem.endsWith("@smartmail.com")) {

                response.getWriter().println("ERROR: Invalid input");
                return;
            }
            toitem = toitem.toLowerCase();
            toListLower.add(toitem);
        }
        String subj = request.getParameter("subj");
        if (subj.length() > 125) {
            response.getWriter().println("ERROR: Invalid input");
            return;
        }
        String content = request.getParameter("content");
        if (content.length() > 250) {
            response.getWriter().println("ERROR: Invalid input");
            return;
        }

        long emailtime = System.currentTimeMillis();
        //Turn the request params into a Mime formatted string for parsing by a Mime parser
        String makeEmail = makeEmail(from, emailtime, toListLower, subj, content);

        //STAC: We write the email to disk -- later we delete it, when we have finished procesing
        String timeString = Long.toString(emailtime);
        clean("./mail");
        File email = new File("mail/" + timeString + ".mime");
        PrintWriter out = new PrintWriter(email);
        out.print(makeEmail);
        out.close();

        try {
            //STAC:Call the MapReducer
            PipelineController.main(null);

        } catch (EmailParseException ex) {
            Logger.getLogger(EmailSenderReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (email.exists()) {
            //STAC:Clean up the email, it was sent out and analyzed successfully
            email.delete();
        }

        response.getWriter().println("OK");

    }

    public String makeEmail(String from, long time, List<String> tofields, String subj, String content) {

        //TODO
        StringBuilder mimemessage = new StringBuilder();

        mimemessage.append("MIME-Version: 1.0\n");
        Date date = new Date(time);
        //mimemessage.append("Date: " + date.toString()+"\n");
        mimemessage.append("From: NAME <" + from + ">\n");
        Iterator<String> it = tofields.iterator();
        while (it.hasNext()) {
            String t = it.next();
            mimemessage.append("To: NAME <" + t + ">\n");
        }
        mimemessage.append("Subject:" + subj + "\n");
        mimemessage.append("Content-Type: text/plain\n\n");
        mimemessage.append(content + "\n");

        return mimemessage.toString();
    }

}
