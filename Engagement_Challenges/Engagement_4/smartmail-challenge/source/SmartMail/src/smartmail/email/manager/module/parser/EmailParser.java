/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.email.manager.module.parser;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.james.mime4j.dom.BinaryBody;

import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.stream.Field;

public class EmailParser {

    private StringBuffer txtBody;
    private StringBuffer htmlBody;
    private ArrayList<BodyPart> attachments;

    public void parseMessage(String fileName) {
        FileInputStream fis = null;

        txtBody = new StringBuffer();
        htmlBody = new StringBuffer();
        attachments = new ArrayList();

        try {
            //Get stream from file  
            fis = new FileInputStream(fileName);
            //Create message with stream from file  
            //If you want to parse String, you can use:  
            //Message mimeMsg = new Message(new ByteArrayInputStream(mimeSource.getBytes()));  
            Message mimeMsg = null;//new Message(fis);  

            //Get custom header by name  
            Field priorityFld = mimeMsg.getHeader().getField("X-Priority");
            //If header doesn't found it returns null  
            if (priorityFld != null) {
                //Print header value  
                System.out.println("Priority: " + priorityFld.getBody());
            }

            //If message contains many parts - parse all parts  
            if (mimeMsg.isMultipart()) {
                //Multipart multipart = (Multipart) mimeMsg.getBody();
                //parseBodyParts(multipart);
            } else {
                //If it's single part message, just get text body  
                String text = getTxtPart(mimeMsg);
                txtBody.append(text);
            }

            for (BodyPart attach : attachments) {
                String attName = attach.getFilename();
                //Create file with specified name  
                FileOutputStream fos = new FileOutputStream(attName);
                try {
                    //Get attach stream, write it to file  
                    BinaryBody bb = (BinaryBody) attach.getBody();
                    bb.writeTo(fos);
                } finally {
                    fos.close();
                }
            }

        } catch (IOException ex) {
            ex.fillInStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * This method classifies bodyPart as text, html or attached file
     *
     * @param multipart
     * @throws IOException
     */
    private void parseBodyParts(Multipart multipart) throws IOException {
        for (Entity part : multipart.getBodyParts()) {
            /*if (part.isMimeType("text/plain")) {  
             String txt = getTxtPart(part);  
             txtBody.append(txt);  
             } else if (part.isMimeType("text/html")) {  
             String html = getTxtPart(part);  
             htmlBody.append(html);  
             } else if (part.getDispositionType() != null && !part.getDispositionType().equals("")) {  
             //If DispositionType is null or empty, it means that it's multipart, not attached file  
             attachments.add(part);  
             }  */

            //If current part contains other, parse it again by recursion  
            if (part.isMultipart()) {
                parseBodyParts((Multipart) part.getBody());
            }
        }
    }

    /**
     *
     * @param part
     * @return
     * @throws IOException
     */
    private String getTxtPart(Entity part) throws IOException {
        //Get content from body  
        TextBody tb = (TextBody) part.getBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tb.writeTo(baos);
        return new String(baos.toByteArray());
    }

}
