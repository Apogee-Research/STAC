package org.techpoint.buystuff;

import org.techpoint.sale.AuctionManager;
import org.techpoint.sale.AuctionDirector;
import org.techpoint.sale.AuctionMemberAPI;
import org.techpoint.sale.messagedata.AuctionProtoSerializer;
import org.techpoint.sale.messagedata.AuctionSerializer;
import org.techpoint.communications.CommsClient;
import org.techpoint.communications.CommsConnection;
import org.techpoint.communications.CommsRaiser;
import org.techpoint.communications.CommsManager;
import org.techpoint.communications.CommsIdentity;
import org.techpoint.communications.CommsPublicIdentity;
import org.techpoint.communications.CommsServer;
import org.techpoint.communications.Communicator;
import org.techpoint.origin.ProposalAppPlaceMemberAPI;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProposalAppCommsManager implements CommsManager, Communicator{

    private CommsServer server;
    private CommsClient client;
    private final int port;
    private final CommsIdentity identity; // identity of this user
    private Map<CommsPublicIdentity, CommsConnection> connections = new HashMap<CommsPublicIdentity, CommsConnection>();
    private AuctionManager auctionManager;
    private AuctionDirector director;

    public ProposalAppCommsManager(CommsIdentity identity, int port, int maxProposal){
        this.identity = identity;
        this.port = port;
        AuctionSerializer serializer = new AuctionProtoSerializer();
        AuctionMemberAPI memberAPI = new ProposalAppPlaceMemberAPI(identity.takeId());
        this.director = new AuctionDirector(identity, maxProposal, this, serializer);
        this.auctionManager = new AuctionManager(director, this, memberAPI, serializer, port, identity, maxProposal);
    }

    public AuctionDirector fetchDirector(){
        return director;
    }

    /* Receives msgs and responds until exit
    * @throws CommsException
    * @throws IOException
    */
    public void run() throws CommsRaiser, IOException, Exception {
        client = new CommsClient(this, identity);
        server = new CommsServer(port, this, identity, client.fetchEventLoopGroup());
        server.serve();
    }

    public void quit(){
        server.close();
        client.close();
    }

    public void connect(String place, int port) throws CommsRaiser {
        CommsConnection conn = client.connect(place, port);
        newConnection(conn);
    }

    @Override
    public void handle(CommsConnection conn, byte[] msg) throws CommsRaiser {
        auctionManager.handle(conn.obtainTheirIdentity(), msg);
    }

    @Override
    public void newConnection(CommsConnection connection) throws CommsRaiser {
        System.out.println("Connected to " + connection.obtainTheirIdentity().grabId());
        connections.put(connection.obtainTheirIdentity(), connection);
        auctionManager.addMember(connection.obtainTheirIdentity());
    }


    public synchronized void closeConnections() throws CommsRaiser {
        for (CommsConnection conn : connections.values()){
            try{
                conn.close();
            }
            catch(CommsRaiser e){
                System.err.println("Error closing CommsConnection " + conn + "\n" + e.getMessage());
            }
            auctionManager.removeMember(conn.obtainTheirIdentity());
        }
        connections.clear();
    }

    @Override
    public void closedConnection(CommsConnection connection) throws CommsRaiser {
        connections.remove(connection);
        auctionManager.removeMember(connection.obtainTheirIdentity());
    }

    /**
     * send msg to user
     */
    public void transmit(CommsPublicIdentity member, byte[] msg) throws CommsRaiser {
        CommsConnection conn = connections.get(member);
        if (conn==null){
            transmitEntity(member);
        }
        else{
            conn.write(msg);
        }
    }

    private void transmitEntity(CommsPublicIdentity member) throws CommsRaiser {
        throw new CommsRaiser("Unknown user " + member.grabId());
    }

}
