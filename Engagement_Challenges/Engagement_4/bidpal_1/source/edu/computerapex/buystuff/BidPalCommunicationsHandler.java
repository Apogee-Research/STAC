package edu.computerapex.buystuff;

import edu.computerapex.buyOp.BarterHandler;
import edu.computerapex.buyOp.BarterDriver;
import edu.computerapex.buyOp.BarterParticipantAPI;
import edu.computerapex.buyOp.BarterHandlerBuilder;
import edu.computerapex.buyOp.messagedata.BarterProtoSerializer;
import edu.computerapex.buyOp.messagedata.BarterSerializer;
import edu.computerapex.dialogs.CommunicationsClient;
import edu.computerapex.dialogs.CommunicationsConnection;
import edu.computerapex.dialogs.CommunicationsDeviation;
import edu.computerapex.dialogs.CommunicationsHandler;
import edu.computerapex.dialogs.CommunicationsIdentity;
import edu.computerapex.dialogs.CommunicationsPublicIdentity;
import edu.computerapex.dialogs.CommunicationsServer;
import edu.computerapex.dialogs.Communicator;
import edu.computerapex.origin.BidPalHostParticipantAPI;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BidPalCommunicationsHandler implements CommunicationsHandler, Communicator{

    private CommunicationsServer server;
    private CommunicationsClient client;
    private final int port;
    private final CommunicationsIdentity identity; // identity of this user
    private Map<CommunicationsPublicIdentity, CommunicationsConnection> connections = new HashMap<CommunicationsPublicIdentity, CommunicationsConnection>();
    private BarterHandler barterHandler;
    private BarterDriver driver;

    public BidPalCommunicationsHandler(CommunicationsIdentity identity, int port, int maxBid){
        this.identity = identity;
        this.port = port;
        BarterSerializer serializer = new BarterProtoSerializer();
        BarterParticipantAPI participantAPI = new BidPalHostParticipantAPI(identity.obtainId());
        this.driver = new BarterDriver(identity, maxBid, this, serializer);
        this.barterHandler = new BarterHandlerBuilder().fixDriver(driver).setCommunicator(this).setParticipantAPI(participantAPI).fixSerializer(serializer).setPort(port).defineIdentity(identity).setMaxBid(maxBid).generateBarterHandler();
    }

    public BarterDriver pullDriver(){
        return driver;
    }

    /* Receives msgs and responds until exit
    * @throws CommsException
    * @throws IOException
    */
    public void run() throws CommunicationsDeviation, IOException, Exception {
        client = new CommunicationsClient(this, identity);
        server = new CommunicationsServer(port, this, identity, client.takeEventLoopGroup());
        server.serve();
    }

    public void quit(){
        server.close();
        client.close();
    }

    public void connect(String host, int port) throws CommunicationsDeviation {
        CommunicationsConnection conn = client.connect(host, port);
        newConnection(conn);
    }

    @Override
    public void handle(CommunicationsConnection conn, byte[] msg) throws CommunicationsDeviation {
        barterHandler.handle(conn.pullTheirIdentity(), msg);
    }

    @Override
    public void newConnection(CommunicationsConnection connection) throws CommunicationsDeviation {
        System.out.println("Connected to " + connection.pullTheirIdentity().takeId());
        connections.put(connection.pullTheirIdentity(), connection);
        barterHandler.addParticipant(connection.pullTheirIdentity());
    }


    public synchronized void closeConnections() throws CommunicationsDeviation {
        for (CommunicationsConnection conn : connections.values()){
            try{
                conn.close();
            }
            catch(CommunicationsDeviation e){
                System.err.println("Error closing CommsConnection " + conn + "\n" + e.getMessage());
            }
            barterHandler.removeParticipant(conn.pullTheirIdentity());
        }
        connections.clear();
    }

    @Override
    public void closedConnection(CommunicationsConnection connection) throws CommunicationsDeviation {
        connections.remove(connection);
        barterHandler.removeParticipant(connection.pullTheirIdentity());
    }

    /**
     * send msg to user
     */
    public void deliver(CommunicationsPublicIdentity participant, byte[] msg) throws CommunicationsDeviation {
        CommunicationsConnection conn = connections.get(participant);
        if (conn==null){
            deliverSupervisor(participant);
        }
        else{
            conn.write(msg);
        }
    }

    private void deliverSupervisor(CommunicationsPublicIdentity participant) throws CommunicationsDeviation {
        throw new CommunicationsDeviation("Unknown user " + participant.takeId());
    }

}
