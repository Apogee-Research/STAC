package infotrader.userinteraction;

import infotrader.userinteraction.DocumentParser;
import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class SitemapServlet {

    public static void mainx(String[] args) throws Exception {

        Server server = new Server(8988);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        System.out.println("Initializing server, please wait...");
        System.out.println("Indexing all files, could take a minute.");
        System.out.println("Initializing server, please wait...");
        //STAC:The EmailSenderReceiver is a servlet, it handles all HTTP Requests
        context.addServlet(new ServletHolder(new DocumentParser()), "/*");
        System.out.println("Server coming up.");
        server.start();
        
        server.join();
    }

}
