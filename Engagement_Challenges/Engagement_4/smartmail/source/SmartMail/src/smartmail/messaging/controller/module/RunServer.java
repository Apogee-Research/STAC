package smartmail.messaging.controller.module;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class RunServer {

    public static void main(String[] args) throws Exception {

        Server server = new Server(8988);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        //STAC: The EmailSenderReceiver is a servlet, it handles all HTTP Requests
        context.addServlet(new ServletHolder(new EmailSenderReceiver()), "/*");

        server.start();
        server.join();
    }

}
