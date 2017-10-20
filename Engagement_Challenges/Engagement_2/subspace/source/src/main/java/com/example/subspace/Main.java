package com.example.subspace;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import org.subethamail.smtp.server.SMTPServer;

public class Main
{
    public static void main(
        String[] args)
    {
        // Parse command line arguments.
        if (args.length != 1)
        {
            printHelp("missing properties file");
        }
        else if ("-h".equals(args[0]) || "--help".equals(args[0]))
        {
            printHelp(null);
        }
        String propertiesFilename = args[0];

        // Read the configuration file.
        Config config = null;
        try
        {
            config = new Config(propertiesFilename);
        }
        catch (Exception e)
        {
            System.err.format(
                "Error reading properties: %s%n",
                e);
            System.exit(1);
        }

        // Load the database.
        String dbName = config.getProperty("subspace.db");
        if (dbName == null)
        {
            System.err.println("Please configure subspace.db");
            System.exit(1);
        }
        Database db = null;
        try
        {
            db = Database.load(dbName);
        }
        catch (Exception e)
        {
            System.err.format(
                "Error reading the database (%s): %s%n",
                dbName,
                e);
            System.exit(1);
        }

        HTTPServer httpServer = null;
        SMTPServer smtpServer = null;
        try
        {
            // Start the HTTP server.
            try
            {
                httpServer = new HTTPServer(config, db);
                httpServer.start();
            }
            catch (Exception e)
            {
                System.err.format(
                    "Error setting up the HTTP server: %s%n",
                    e);
                System.exit(1);
            }

            // Start the SMTP server.
            try
            {
                SMTPHandler smtpHandler = new SMTPHandler(config, db);
                smtpServer = new SMTPServer(smtpHandler);
                smtpHandler.configure(smtpServer);
                smtpServer.start();
            }
            catch (Exception e)
            {
                System.err.format(
                    "Error setting up the SMTP server: %s%n",
                    e);
                System.exit(1);
            }

            // Everything's running smoothly, let it go.
            while (true)
            {
                try
                {
                    Thread.sleep(1000L);

                    // Catch any changes that forget to call
                    // saveHint() themselves.
                    db.saveHint();
                }
                catch (InterruptedException e)
                {
                }
            }
        }
        finally
        {
            if (httpServer != null)
            {
                httpServer.stop();
            }

            if (smtpServer != null)
            {
                smtpServer.stop();
            }

            try
            {
                db.save();
            }
            catch (IOException e)
            {
                System.err.format(
                    "Error saving the database: %s%n",
                    e);
                System.exit(1);
            }
        }
    }

    private static void printHelp(
        String errorMsg)
    {
        PrintStream prt =
            errorMsg == null
                ? System.out
                : System.err
                ;

        if (errorMsg != null)
        {
            prt.format("Error: %s%n", errorMsg);
            prt.println();
        }

        prt.format(
            "Usage: %s <properties-file>%n",
            Main.class.getName());
        prt.println();

        prt.println("See the example subspace.properties file");
        prt.println("distributed with this application. Edit that");
        prt.println("as directed by the comments.");

        System.exit(errorMsg == null ? 0 : 1);
    }
}
