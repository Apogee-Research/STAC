/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collab;

/**
 *
 * @author user
 */
public class RunCollab {

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println("Choose to run the client or the server by entering command:java -jar Collab.jar [server|client]");
        }

        if (args[0].equalsIgnoreCase("server")) {
            DistFSysServer.main(args);

        } else if (args[0].equalsIgnoreCase("client")) {
            client.CommandLineApp.main(args);
        } else {
            System.out.println("Choose to run the client or the server by entering command:java -jar Collab.jar [server|client]");

        }

    }

}
