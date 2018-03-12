package collab;

import collab.dstructs.BTree;
import collab.dstructs.objs.NormalUserData;
import collab.dstructs.DotNodeCallBack;
import collab.dstructs.objs.AuditorData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollabServer extends SimpleChannelInboundHandler<DatagramPacket> {

    //The operation ID bytes for the UDP packets
    public static final int LOGIN = 23;
    public static final int ADD = 3;
    public static final int SEARCHMAIN = 10;
    public static final int SEARCHSANDBOX = 11;
    public static final int INITSANDBOX = 13;
    public static final int COMMITSANDBOX = 14;
    public static final int DESTROYSANDBOX = 15;
    public static final Integer MINVAL = 100000;

    //Constant offset to ensure large ids for user ids, 
    public static long userminid = Integer.MAX_VALUE + Integer.MAX_VALUE;

    //The main public btreeformastercalendar, does not contain vulnerability
    private final BTree btreeformastercalendar;

    //Session id for user interaction to save state
    //<Session id number, SessionData>
    Map<Integer, SessionData> sessionmap = new HashMap<Integer, SessionData>();
    //<Username, SessionData>
    Map<String, SessionData> usermap = new HashMap<String, SessionData>();
    //This map stores a sandbox state. The key is the session id  <SessionID, Sandbox>
    Map<Integer, SchedulingSandbox> sandboxes = new HashMap<Integer, SchedulingSandbox>();

    //Used to generate session ids
    static Random srand = new Random(100);
    //<Username, AuditID> A map from the normal user name to the audit event id
    private HashMap<String, Integer> auditids;

    //Holds information to keep track of user state
    public class SessionData {

        long id;
        int sessionid;
        String username;
        boolean islocked;

        public SessionData(String u) {
            username = u;
            islocked = false;
            //2147483647L is the Integer.MAXINT
            //This is to allow the packing of two vars into one one. The user ID occupies the 
            //upper 32 bits, and the event id the lower 32 -- NOT important for vulnerability
            userminid = userminid + 2147483647L;
            //userminid is a glabal state value, so it needs to be assigned seperately
            id = userminid;
            //Store this users information in global state
            usermap.put(u, this);
            //Generate a positive session id
            sessionid = srand.nextInt(Integer.MAX_VALUE - 1);
            //Store this session for easy retrieval
            sessionmap.put(sessionid, this);

        }

        //A simple Mutex style lock, cooperativel controls access to operations 
        public synchronized boolean checkandlock() {

            if (!islocked) {
                islocked = true;
                return islocked;
            } else {
                return islocked;
            }
        }

        private void releaselock() {
            islocked = false;
        }

    }

    public CollabServer() throws IOException {

        userminid = Integer.MAX_VALUE;
        //Ininiate the master calendar
        btreeformastercalendar = new BTree();

        //Loads the canned calendar data, user data, audit events
        loadData();

        System.out.println("Collab UDP Server Up and Running");
    }

    @Override
    //Reads the udp packet fomr the client
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        //the buffer for responses
        ByteBuf bos = null;
        ByteBufAllocator alloc = PooledByteBufAllocator.DEFAULT;

        //Get the operation id byte
        byte t = packet.content().getByte(0);

        int status = -1;
        try {
            switch (t) {
                case CollabServer.ADD: {
                    //pos tracks the position in the UDP packet
                    int pos = 1;
                    Integer sessionid = packet.content().getInt(pos);
                    //Increment position in UDP packet by 4
                    //The shift right converts from bits to bytes
                    pos += Integer.SIZE >> 3;
                    //Get our sandbox for this sesssion
                    SchedulingSandbox sbox = this.sandboxes.get(sessionid);
                    //This is the value of the event id to add to sandbox tempindex
                    Integer val = packet.content().getInt(pos);
                    boolean add = true;

                    if (val > MINVAL) {

                        try {
                            //Add the event and check for DuplicateKeyException
                            add = sbox.add(val, new NormalUserData());
                        } catch (DuplicateKeyException e) {
                        //STAC:The throwing of a DuplicateKeyException indicates
                            //we have received the audit ID
                            status = -5;
                            //This does not kill the server
                            System.out.println("val:" + val + " " + e.getMessage());
                            throw new DuplicateKeyException();
                        }
                    } else {
                            status = -2;
                            //This does not kill the server
                            System.out.println("val is too small");
                            add = false;
                            throw new InvalidValueException("val is too small");
                        
                    }
                    //Let the client know all is well
                    writeSuccessExpected(bos, ctx, alloc, packet);
                    //TODO:THIS printDot and println CODE MUST BE REMOVED -- 
                    //System.out.println("v:" + val);
                    //sbox.printDot();
                    //System.out.println("add:" + add);
                    //System.out.println("Dot-call:" + DotNodeCallBack.outnum);
                    byte b_b = add ? (byte) 1 : (byte) 0;;
                    bos = alloc.directBuffer(1);
                    bos.writeByte(b_b);
                    ctx.write(new DatagramPacket(bos, packet.sender()));
                    ctx.flush();
                }
                break;

                //GET ALL PUBLIC EVENT IDS
                case CollabServer.SEARCHSANDBOX: {
                    //pos tracks the position in the UDP packet
                    int pos = 1;
                    Integer sessionid = packet.content().getInt(pos);
                    pos += Integer.SIZE >> 3;
                    SchedulingSandbox sbox = this.sandboxes.get(sessionid);
                    Integer min = packet.content().getInt(pos);
                    pos += Integer.SIZE >> 3;
                    Integer max = packet.content().getInt(pos);
                    pos += Integer.SIZE >> 3;

                    //Perform the search
                    EventResultSet eres = sbox.getRange(min, max);
                    List<Integer> range = eres.get();

                    //Just acknowledgement to client that all is well, so far, sdata ready for results
                    writeSuccessExpected(bos, ctx, alloc, packet);
                    //TODO: Wheer is the number of ids returned?????????
                    for (int i = 0; i < range.size(); i++) {
                        bos = alloc.directBuffer(4);
                        bos.writeInt(range.get(i));
                        //TODO: Remove println or reppace with with logging level
                        //System.out.println("range.get(i): " + range.get(i));
                        ctx.write(new DatagramPacket(bos, packet.sender()));

                    }
                    //Let the client know we are done with -8
                    bos = alloc.directBuffer(4);
                    bos.writeInt(-8);
                    ctx.write(new DatagramPacket(bos, packet.sender()));
                    ctx.flush();
                }
                break;

                case CollabServer.LOGIN: {
                    //pos tracks the position in the UDP packet
                    int pos = 1;
                    //The size of the username string
                    Integer sizeofdata = packet.content().getInt(pos);
                    pos += Integer.SIZE >> 3;
                    int startpos = pos;
                    //BUILD THE USERNAME STRING
                    StringBuilder uname = new StringBuilder();
                    for (; pos < startpos + sizeofdata * (Character.SIZE >> 3); pos += (Character.SIZE >> 3)) {
                        char c = (char) packet.content().getChar(pos);
                        uname.append(c);
                    }

                    SessionData session = usermap.get(uname.toString());

                    if (session != null) {
                        writeSuccessExpected(bos, ctx, alloc, packet);
                    }
                    //TODO: BUG : WHAT IF SESSION IS NULL

                    bos = alloc.directBuffer(4);
                    bos.writeInt(session.sessionid);

                    ctx.write(new DatagramPacket(bos, packet.sender()));
                    ctx.flush();

                }
                break;
                case CollabServer.SEARCHMAIN: {
                    int pos = 1;
                    //Needs the session username to know what user's data to search for
                    Integer sizeofdata = packet.content().getInt(pos);
                    pos += Integer.SIZE >> 3;
                    int startpos = pos;
                    //BUILD THE USERNAME STRING
                    StringBuilder uname = new StringBuilder();
                    for (; pos < startpos + sizeofdata * (Character.SIZE >> 3); pos += (Character.SIZE >> 3)) {
                        char c = (char) packet.content().getChar(pos);
                        uname.append(c);
                    }
                    Integer min = packet.content().getInt(pos);
                    Long lmin = Long.parseLong(String.valueOf(min));
                    pos += Integer.SIZE >> 3;
                    Integer max = packet.content().getInt(pos);
                    Long lmax = Long.parseLong(String.valueOf(max));
                    pos += Integer.SIZE >> 3;
                    SessionData sdata = usermap.get(uname.toString());
                    //Packing userid with the event id into one long sized value
                    long newidmin = sdata.id + (lmin);
                    long newidmax = sdata.id + (lmax);
                    //EventResultSet eres2 = btreeformastercalendar.getRange(sdata.id, newidmin, newidmax);

                    EventResultSet eres =btreeformastercalendar.searchR(sdata.id, newidmin, newidmax);
                                        
                    writeSuccessExpected(bos, ctx, alloc, packet);

                    List<Integer> range = eres.get();
                    for (int i = 0; i < range.size(); i++) {
                        bos = alloc.directBuffer(4);
                        bos.writeInt(range.get(i));
                        //TODO: Remove println or reppace with with logging level
                        //System.out.println("range.get(i): " + range.get(i));
                        ctx.write(new DatagramPacket(bos, packet.sender()));

                    }
                    //Let the client know we are done with -8
                    bos = alloc.directBuffer(4);
                    bos.writeInt(-8);
                    ctx.write(new DatagramPacket(bos, packet.sender()));
                    ctx.flush();
                }
                break;

                case CollabServer.INITSANDBOX: {
                    int pos = 1;
                    Integer sessionid = packet.content().getInt(pos);
                    SessionData sdata = sessionmap.get(sessionid);
                    //Acquire lock
                    if (sdata.checkandlock()) {
                        EventResultSet range = btreeformastercalendar.searchR(sdata.id, sdata.id + 1L, sdata.id + 2147483647L - 1L);
                        //Packing the min and max with the user id into one long sized value
                        //EventResultSet range2 = btreeformastercalendar.getRange(sdata.id, sdata.id + 1L, sdata.id + 2147483647L - 1L);

                        //Insert data into sandxon
                        SchedulingSandbox sbox = SchedulingSandbox.populateSandbox(range);
                        //Get the auditor data
                        //TODO: remove printDot
                        //sbox.printDot();
                        //Get the single audit id associated with user, if there is one
                        Integer audid = auditids.get(sdata.username);
                        if (audid != null) {
                            sbox.add(audid, new AuditorData());
                        }

                        //We are done loading startup data, put sandbxox into 'New Insertion Mode' (see design doc)
                        sbox.initSandbox();
                        //
                        sandboxes.put(sdata.sessionid, sbox);

                        writeSuccessExpected(bos, ctx, alloc, packet);

                        //Double acknowledge -- pointless, but it hurts nothing
                        //Left in for potential future feature
                        bos = alloc.directBuffer(4);
                        bos.writeInt(1);

                        ctx.write(new DatagramPacket(bos, packet.sender()));
                        ctx.flush();
                    } else {
                        //No system errors, send 1 as ack
                        writeSuccessExpected(bos, ctx, alloc, packet);
                        bos = alloc.directBuffer(4);
                        //but we did not acquire lock, so send -1
                        bos.writeInt(-1);

                        ctx.write(new DatagramPacket(bos, packet.sender()));
                        ctx.flush();
                    }
                }
                break;

                //Pushed data back to master calendar -- not important for vulnerability
                case CollabServer.COMMITSANDBOX: {
                    int pos = 1;
                    Integer sessionid = packet.content().getInt(pos);
                    SessionData sdata = sessionmap.get(sessionid);
                    SchedulingSandbox sbox = sandboxes.get(sdata.sessionid);
                    if (sbox != null && sdata.checkandlock()) {

                        //Integer audid = auditids.get(sdata.username);
                        //sbox.commit();
                        EventResultSet eres = sbox.getRange(0, Integer.MAX_VALUE);
                        List<Integer> range = eres.get();
                        Iterator<Integer> it = range.iterator();
                        while (it.hasNext()) {

                            Integer nextv = it.next();
                            /*if(audid.intValue()==nextv.intValue()){
                                System.out.println("SKIP AUDIT ID");
                                continue;
                            }*/
                            Long nextvl = Long.parseLong(String.valueOf(nextv));
                            nextvl = sdata.id + nextvl;
                            Object search = btreeformastercalendar.search(nextvl);
                            if(search!=null)
                                btreeformastercalendar.delete(nextvl);
                            //System.out.println("fromsbox nextvl"+nextv);
                            btreeformastercalendar.add(nextvl, nextvl, true);

;                        }
                        //No system errors, send 1 as ack
                        writeSuccessExpected(bos, ctx, alloc, packet);

                        bos = alloc.directBuffer(4);
                        bos.writeInt(1);

                        ctx.write(new DatagramPacket(bos, packet.sender()));
                        ctx.flush();
                    } else {
                        //No system errors, send 1 as ack
                        writeSuccessExpected(bos, ctx, alloc, packet);
                        bos = alloc.directBuffer(4);
                        bos.writeInt(-1);

                        ctx.write(new DatagramPacket(bos, packet.sender()));
                        ctx.flush();
                    }
                }
                break;
                //Rejects data, do not Push data back to master calendar -- not important for vulnerability
                case CollabServer.DESTROYSANDBOX: {
                    int pos = 1;
                    Integer sessionid = packet.content().getInt(pos);
                    SessionData sdata = sessionmap.get(sessionid);

                    sandboxes.put(sdata.sessionid, null);
                    //We no longer need, why not tell the jvm to garbage collect
                    System.gc();

                    //This will release the lock even if you do not have control of it -- it is a cooperative system
                    sdata.releaselock();
                    writeSuccessExpected(bos, ctx, alloc, packet);

                    bos = alloc.directBuffer(4);
                    bos.writeInt(1);

                    ctx.write(new DatagramPacket(bos, packet.sender()));
                    ctx.flush();
                }
                break;

                default:
                    //byte b_b = (byte) 0;
                    bos = alloc.directBuffer(4);
                    bos.writeInt(-1);
                    break;

            }
            //System.gc();
        } catch (Exception e) {
            //Just catch everything, runtime all of it

            bos = alloc.directBuffer(4);
            bos.writeInt(status);
            ctx.write(new DatagramPacket(bos, packet.sender()));
            ctx.flush();
            Logger.getLogger(CollabServer.class.getName()).log(Level.SEVERE, e.getMessage(), e);

        }
        
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        //ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    }

    private void loadData() throws IOException {

        File logs = new File("logs");
        logs.mkdir();
        delete(logs);

        //TODO: Make these a resuorce and pack inside the jar!!!!!!!
        loadUserFile("user.data");
        loadEventFile("event.data");
        loadAuditFile("audit.data");

    }

    void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        } else if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }

    public void loadUserFile(String fname) throws IOException {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("data/" + fname);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            for (String line; (line = br.readLine()) != null;) {
                int sIndexOf = line.lastIndexOf(',');
                String user = line.substring(0, sIndexOf);
                // process the line.
                SessionData sd1 = new SessionData(user);
                usermap.put(user, sd1);
            }
        }
    }

    public void loadEventFile(String fname) throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("data/" + fname);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            for (String line; (line = br.readLine()) != null;) {
                int sIndexOf = line.lastIndexOf(',');
                String user = line.substring(0, sIndexOf);
                String id = line.substring(sIndexOf + 1, line.length());
                // process the line.
                SessionData sd = usermap.get(user);
                long parsedLong = Long.parseLong(id);
                long storeid = sd.id + parsedLong;
                btreeformastercalendar.add(storeid, null, true);
            }
        }
    }

    public void loadAuditFile(String fname) throws IOException {
        auditids = new HashMap<String, Integer>();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("data/" + fname);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            for (String line; (line = br.readLine()) != null;) {
                int sIndexOf = line.lastIndexOf(',');
                String user = line.substring(0, sIndexOf);
                String id = line.substring(sIndexOf + 1, line.length());

                int auditorid = Integer.parseInt(id);
                auditids.put(user, auditorid);

            }
        }
    }

    private void writeSuccessExpected(ByteBuf bos, ChannelHandlerContext ctx, ByteBufAllocator alloc, DatagramPacket packet) {
        bos = alloc.directBuffer(4);
        bos.writeInt(1);

        ctx.write(new DatagramPacket(bos, packet.sender()));
        ctx.flush();
    }
}
