/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.support.extra;

/**
 *
 * @author user
 */
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;

/**
 * MyCustomHandler outputs contents to a specified file
 */
public class LogBuffer extends Handler {

    FileOutputStream fileOutputStream;
    PrintWriter printWriter;
    CharBuffer buf;
    String filename;

    String[] buffx = new String[Character.SIZE >> 3];
    int buffxloc = 0;



    public LogBuffer(String fname) {
        super();


        filename = fname;
        // check input parameter
        if (filename == null || filename == "") {
            filename = "translogfile.txt";
        }
       
    }

    /* (non-API documentation)
     * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
     */
    public void publish(LogRecord record) {
        // ensure that this log record should be logged by this Handler
        if (!isLoggable(record)) {
            return;
        }
        buf = CharBuffer.allocate(Character.SIZE >> 3);
        try {
            // initialize the file 
            fileOutputStream = new FileOutputStream(filename);
            printWriter = new PrintWriter(fileOutputStream);
            setFormatter(new XMLFormatter());
        } catch (Exception e) {
            // implement exception handling...
        }
        try {
            buf.put(getFormatter().format(record));
        } catch (java.nio.BufferOverflowException boe) {
            System.out.println("logging");
            flush();
            printWriter.println(getFormatter().format(record));

        }

        // Output the formatted data to the file
    }

    public void publish(String m) {
        // ensure that this log record should be logged by this Handler
        try {
            buffx[buffxloc] = m;
            buffxloc++;
        } catch (ArrayIndexOutOfBoundsException e) {

            for (int i = 0; i < buffx.length; i++) {
                java.util.logging.LogRecord lr = new java.util.logging.LogRecord(java.util.logging.Level.INFO, buffx[i]);
                publish(lr);
            }
            java.util.logging.LogRecord lr = new java.util.logging.LogRecord(java.util.logging.Level.INFO, m);
            publish(lr);
            buffxloc = 0;
        }
        // Output the formatted data to the file
    }

    /* (non-API documentation)
     * @see java.util.logging.Handler#flush()
     */
    public void flush() {
        char[] array = buf.array();

        printWriter.print(array);
        printWriter.flush();
        buf.clear();
    }

    /* (non-API documentation)
     * @see java.util.logging.Handler#close()
     */
    public void close() throws SecurityException {
        printWriter.close();
    }
}
