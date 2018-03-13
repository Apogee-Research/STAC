/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.email.manager.module.parser;

import smartmail.datamodel.EmailAddress;
import smartmail.datamodel.EmailEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.codec.DecoderUtil;
import org.apache.james.mime4j.dom.address.Address;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.address.MailboxList;
import org.apache.james.mime4j.dom.field.AddressListField;
import org.apache.james.mime4j.dom.field.DateTimeField;
import org.apache.james.mime4j.dom.field.MailboxListField;
import org.apache.james.mime4j.dom.field.ParsedField;
import org.apache.james.mime4j.dom.field.UnstructuredField;
import org.apache.james.mime4j.field.LenientFieldParser;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;

import org.xml.sax.SAXException;

public class MailContentHandler implements ContentHandler {

    private boolean strictParsing = false;

    HashMap<String, String> mdata;

    private boolean inPart = false;

    EmailEvent email;

    public MailContentHandler(boolean strictParsing, EmailEvent email) {

        this.strictParsing = strictParsing;

        this.email = email;

        mdata = new HashMap<String, String>();

    }

    public void body(BodyDescriptor body, InputStream is) throws MimeException,
            IOException {

        mdata.put("CONTENT_TYPE", body.getMimeType());
        mdata.put("CONTENT_ENCODING", body.getCharset());

        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(is, "UTF-8");
        for (;;) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0) {
                break;
            }
            out.append(buffer, 0, rsz);
        }
        String bodystr = out.toString();
        System.out.println("bodystr:" + bodystr);
        //STAC: Parse out the body and put in EmailEvent object
        email.setContent(bodystr);

        /*try {
         if (extractor.shouldParseEmbedded(submd)) {
         extractor.parseEmbedded(is, handler, submd, false);
         }
         } catch (SAXException e) {
         throw new MimeException(e);
         }*/
    }

    public void endBodyPart() throws MimeException {

    }

    public void endHeader() throws MimeException {
    }

    public void startMessage() throws MimeException {

    }

    public void endMessage() throws MimeException {

    }

    public void endMultipart() throws MimeException {
        inPart = false;
    }

    public void epilogue(InputStream is) throws MimeException, IOException {
    }

    public void field(Field field) throws MimeException {
        // inPart indicates whether these metadata correspond to the
        // whole message or its parts
        if (inPart) {
            return;
        }

        try {
            String fieldname = field.getName();
            ParsedField parsedField = LenientFieldParser.getParser().parse(
                    field, DecodeMonitor.SILENT);
            if (fieldname.equalsIgnoreCase("From")) {
                MailboxListField fromField = (MailboxListField) parsedField;
                MailboxList mailboxList = fromField.getMailboxList();
                //STAC: Parse out the from field and put in EmailEvent object
                if (fromField.isValidField() && mailboxList != null) {
                    for (Address address : mailboxList) {
                        String from = getDisplayString(address);
                        mdata.put("MESSAGE_FROM", from);
                        smartmail.datamodel.EmailAddress efrom = new smartmail.datamodel.EmailAddress(0);
                        efrom.setValue(address.toString());
                        efrom.setProtocol(fieldname);
                        email.setSource(efrom);
                    }
                } else {
                    String from = stripOutFieldPrefix(field, "From:");
                    if (from.startsWith("<")) {
                        from = from.substring(1);
                    }
                    if (from.endsWith(">")) {
                        from = from.substring(0, from.length() - 1);
                    }
                    mdata.put("MESSAGE_FROM", from);
                    smartmail.datamodel.EmailAddress efrom = new smartmail.datamodel.EmailAddress(0);
                    efrom.setValue((from));
                    efrom.setProtocol(fieldname);
                    email.setSource(efrom);
                }
            } else if (fieldname.equalsIgnoreCase("Subject")) {
                //STAC: Parse out the subject field and put in EmailEvent object
                email.setSubject(((UnstructuredField) parsedField).getValue());
                mdata.put("TRANSITION_SUBJECT_TO_DC_TITLE",
                        ((UnstructuredField) parsedField).getValue());
            } else if (fieldname.equalsIgnoreCase("To")) {
                processAddressList(parsedField, "To:", "MESSAGE_TO");
            } else if (fieldname.equalsIgnoreCase("CC")) {
                processAddressList(parsedField, "Cc:", "MESSAGE_CC");
            } else if (fieldname.equalsIgnoreCase("BCC")) {
                processAddressList(parsedField, "Bcc:", "MESSAGE_BCC");
            } else if (fieldname.equalsIgnoreCase("Date")) {
                DateTimeField dateField = (DateTimeField) parsedField;
                mdata.put("CREATED", dateField.getDate().toString());
            }
        } catch (RuntimeException me) {
            if (strictParsing) {
                throw me;
            }
        } catch (IOException ex) {
            Logger.getLogger(MailContentHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static int inc = 0;

    private void processAddressList(ParsedField field, String addressListType,
            String metadataField) throws MimeException, IOException {
        AddressListField toField = (AddressListField) field;

        if (toField.isValidField()) {
            //STAC: Parse out the to field and put in EmailEvent object
            AddressList addressList = toField.getAddressList();
            for (Address address : addressList) {
                Mailbox m = (Mailbox) address;
                mdata.put(metadataField, getDisplayString(address));
                smartmail.datamodel.EmailAddress efrom = new smartmail.datamodel.EmailAddress(0);
                efrom.setValue(m.toString());
                EmailAddress e = efrom.checkAddress(efrom);
                email.addOtherdestination(e);
            }
        } else {
            String to = stripOutFieldPrefix(field,
                    addressListType);
            for (String eachTo : to.split(",")) {
                mdata.put(metadataField, eachTo.trim());
                smartmail.datamodel.EmailAddress efrom = new smartmail.datamodel.EmailAddress(0);
                efrom.setValue(eachTo.trim());
                efrom.setProtocol(addressListType);
                email.addOtherdestination(efrom);

            }
        }
    }

    private String getDisplayString(Address address) {
        if (address instanceof Mailbox) {
            Mailbox mailbox = (Mailbox) address;
            String name = mailbox.getName();
            if (name != null && name.length() > 0) {
                name = DecoderUtil.decodeEncodedWords(name, DecodeMonitor.SILENT);
                return name + " ";
            } else {
                return mailbox.getAddress();
            }
        } else {
            return address.toString();
        }
    }

    public void preamble(InputStream is) throws MimeException, IOException {
    }

    public void raw(InputStream is) throws MimeException, IOException {

        System.out.println(is.available());
    }

    public void startBodyPart() throws MimeException {
        /*try {
         handler.startElement("div", "class", "email-entry");
         handler.startElement("p");
         } catch (SAXException e) {
         throw new MimeException(e);
         }*/
    }

    public void startHeader() throws MimeException {
        // TODO Auto-generated method stub

    }

    public void startMultipart(BodyDescriptor descr) throws MimeException {
        inPart = true;
        System.out.println("startMultipart:" + descr.getContentLength());
    }

    private String stripOutFieldPrefix(Field field, String fieldname) {
        String temp = field.getRaw().toString();
        int loc = fieldname.length();
        while (temp.charAt(loc) == ' ') {
            loc++;
        }
        return temp.substring(loc);
    }

}
