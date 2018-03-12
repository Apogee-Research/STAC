package client;

import collab.CollabServer;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DStoreClient {

    public static final int PORT = 7688;//Integer.parseInt(System.getProperty("port", default_port));
    static HashMap<Integer, Long> out_Times = new HashMap<Integer, Long>();

    public static HashMap<Integer, Long> finish_times = new HashMap<Integer, Long>();
    public static HashMap<Integer, Boolean> node_split = new HashMap<Integer, Boolean>();
    public static HashMap<Integer, Boolean> beenUsed = new HashMap<Integer, Boolean>();
    private static final int SUCCESS = 1;
    private int status;
    public long lasttiming = 0;
    public boolean lastsplit = false;

    public DStoreClient() {

    }
    
    public int getLastStatus(){
        return status;
    }

    private DatagramSocket clientSocket;
    private InetAddress IPAddress;

    public void connect(String ipAddress, int port) throws SocketException, UnknownHostException {

        String serverHostname = new String(ipAddress);
        clientSocket = new DatagramSocket();
        IPAddress = InetAddress.getByName(serverHostname);

    }

    public boolean insertnewkey(int sessionid, int key) throws IOException, CollabConnException {
        byte[] receiveData = new byte[5];

        // Pack random number into byte array and then packet
        ByteBuffer b = ByteBuffer.allocate(9);
        b.put((byte) CollabServer.ADD);
        b.putInt(sessionid);
        b.putInt(key);

        byte[] recv = sendtime(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        byte status = contents.get();
        boolean stat = (status != 0);
        lastsplit = stat;
        return stat;
    }

    public boolean insertnewkeyfast(int sessionid, int key/*, String desc*/) throws IOException, CollabConnException {
        byte[] receiveData = new byte[5];

        // Pack random number into byte array and then packet
        ByteBuffer b = ByteBuffer.allocate(1 + (Integer.SIZE >> 3)
                + (Integer.SIZE >> 3)
                //+ (Integer.SIZE >> 3)
                //+ (desc.length() * (Character.SIZE >> 3))
        );

        b.put((byte) CollabServer.ADD);
        b.putInt(sessionid);
        b.putInt(key);
        
        /*b.putInt(desc.length());
        for (int i = 0; i < desc.length(); i++) {
            b.putChar(desc.charAt(i));
        }*/

        byte[] recv = sendtime(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        byte status = contents.get();
        boolean stat = (status != 0);
        lastsplit = stat;
        return stat;
    }

    public byte delete(int key) throws IOException, CollabConnException {
        byte[] receiveData = new byte[5];

        // Pack random number into byte array and then packet
        ByteBuffer b = ByteBuffer.allocate(5);
        b.put((byte) 12);
        b.putInt(key);

        byte[] recv = send(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        byte status = contents.get();
        return status;
    }

    public void update(int key, String rname) throws IOException, CollabConnException {
        byte[] receiveData = new byte[1];

        ByteBuffer b = ByteBuffer.allocate((Byte.SIZE >> 3) + (Integer.SIZE >> 3) + (Integer.SIZE >> 3) + (rname.length() * (Character.SIZE >> 3)));
        b.put((byte) 9);
        b.putInt(key);
        b.putInt(rname.length());
        for (int i = 0; i < rname.length(); i++) {
            b.putChar(rname.charAt(i));
        }

        byte[] recv = send(b, receiveData);
    }

    private byte[] send(ByteBuffer b, byte[] receiveData) throws IOException, CollabConnException {

        byte[] array = b.array();
        DatagramPacket sendPacket = new DatagramPacket(array, array.length, IPAddress, PORT);
        // Send packet
        clientSocket.send(sendPacket);

        int status = getstatus();
        if (status != SUCCESS) {
            throw new CollabConnException("Exception Occurred");
        }
        // Start waiting for recv
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        // Recv has happened here
        byte[] recv = receivePacket.getData();
        return recv;

    }

    public int getstatus() throws IOException {
        byte[] status = new byte[4];
        DatagramPacket receivePacket = new DatagramPacket(status, status.length);
        clientSocket.receive(receivePacket);

        byte[] data = receivePacket.getData();
        if (data == null || data.length == 0) {
            return -1;
        }
        ByteBuffer contents = ByteBuffer.wrap(data, 0, data.length);
        int v = contents.getInt();

        return v;
    }

    private byte[] sendtime(ByteBuffer b, byte[] receiveData) throws IOException, CollabConnException {

        byte[] array = b.array();
        DatagramPacket sendPacket = new DatagramPacket(array, array.length, IPAddress, PORT);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        // Send packet
        
        byte[] statusb = new byte[4];
        DatagramPacket receivePacketS = new DatagramPacket(statusb, statusb.length);
        
        clientSocket.send(sendPacket);
        

        clientSocket.receive(receivePacketS);
        
        byte[] data = receivePacketS.getData();
        if (data == null || data.length == 0) {
            status =  -1;
        } else {
            ByteBuffer contents = ByteBuffer.wrap(data, 0, data.length);
            status = contents.getInt();
        }
        if (status != SUCCESS) {
            
            System.out.println("status: "+status);
            throw new CollabConnException("Exception Occurred:"+status);
        }
        // Start waiting for recv
        clientSocket.receive(receivePacket);
        

        // Recv has happened here
        byte[] recv = receivePacket.getData();
        return recv;

    }

    private void sendget(ByteBuffer b, byte[] buf, ArrayList<Integer> vals) throws IOException, CollabConnException {

        /*b.position(1);
         int aInt = b.getInt();
         int bInt = b.getInt();*/
        byte[] array = b.array();
        DatagramPacket sendPacket = new DatagramPacket(array, array.length, IPAddress, PORT);
        // Send packet
        clientSocket.send(sendPacket);
        int status = getstatus();
        if (status != SUCCESS) {
            throw new CollabConnException("Exception Occurred");
        }
        // Start waiting for recv
        /*DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
         clientSocket.receive(receivePacket);
         // Recv has happened here
         byte[] recv = receivePacket.getData();
         return recv;*/
        System.out.println("status:waiting on results from server");
        boolean keepGoing = true;
        while (keepGoing) {
            DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
            
            clientSocket.receive(receivePacket);
            //System.out.println("status:receiving results from server");
            //System.out.println("getData");
            byte[] data = receivePacket.getData();
            if (data == null || data.length == 0) {
                break;
            }
            ByteBuffer contents = ByteBuffer.wrap(data, 0, data.length);
            int v = contents.getInt();

            //System.out.println("v:" + v);
            //if the packet is empty or null, then the server is done sending?
            if (v == -8) {

                keepGoing = false;
                System.out.println("status:done receiving results from server");
            } else {
                vals.add(v);
            }
        }

    }

    private String sendget(ByteBuffer b, byte[] buf) throws IOException {

        byte[] array = b.array();
        DatagramPacket sendPacket = new DatagramPacket(array, array.length, IPAddress, PORT);
        // Send packet
        clientSocket.send(sendPacket);

        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
        clientSocket.receive(receivePacket);

        int length = receivePacket.getLength();
        //byte[] data = new byte[length];
        byte[] data = receivePacket.getData();

        String datastr = new String(data, 0, length, "UTF-8");

        return datastr;

    }

    public boolean beginTransaction() throws IOException, CollabConnException {
        byte[] receiveData = new byte[1];

        ByteBuffer b = ByteBuffer.allocate(1);
        b.put((byte) 7);
        byte[] recv = send(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        byte status = contents.get();
        boolean res = (boolean) (status != 0);
        return res;
    }

    public boolean add(int sessionid, int k, int d, boolean b) throws IOException, CollabConnException {

        boolean ret = insertnewkey(sessionid, k);
        //insertdata(k, d);
        return ret;
    }

    public void commit() throws IOException, CollabConnException {
        byte[] receiveData = new byte[1];

        ByteBuffer b = ByteBuffer.allocate(1);
        b.put((byte) 4);
        byte[] recv = send(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        byte status = contents.get();
    }

    public void rollback() throws IOException, CollabConnException {
        byte[] receiveData = new byte[1];

        ByteBuffer b = ByteBuffer.allocate(1);
        b.put((byte) 5);
        byte[] recv = send(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        byte status = contents.get();
    }

    public void fastInsert(boolean onoff) throws IOException, CollabConnException {
        byte[] receiveData = new byte[1];

        ByteBuffer b = ByteBuffer.allocate(2);
        b.put((byte) 2);
        b.put((byte) (onoff ? 1 : 0));
        byte[] recv = send(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        byte status = contents.get();
    }

    /*public void printsecretnode() throws IOException {
     byte[] receiveData = new byte[1];

     ByteBuffer b = ByteBuffer.allocate(1);
     b.put((byte) 7);
     byte[] recv = send(b, receiveData);

     ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
     byte status = contents.get();
     }*/
    public ArrayList<Integer> search(int sessionid, int min, int max) throws IOException, CollabConnException {
        byte[] receiveData = new byte[4];

        ByteBuffer b = ByteBuffer.allocate(13);
        b.put((byte) CollabServer.SEARCHSANDBOX);
        b.putInt(sessionid);
        b.putInt(min);
        b.putInt(max);

        ArrayList<Integer> vals = new ArrayList<Integer>();
        sendget(b, receiveData, vals);

        return vals;
    }

    public void storefile(String rname, String fcontents) throws IOException, CollabConnException {
        byte[] receiveData = new byte[1];

        ByteBuffer b = ByteBuffer.allocate((Byte.SIZE >> 3) + (Integer.SIZE >> 3) + (rname.length() * (Character.SIZE >> 3)) + (Integer.SIZE >> 3) + (fcontents.length() * (Character.SIZE >> 3)));
        b.put((byte) 11);
        b.putInt(rname.length());
        for (int i = 0; i < rname.length(); i++) {
            b.putChar(rname.charAt(i));
        }
        b.putInt(fcontents.length());
        for (int i = 0; i < fcontents.length(); i++) {
            b.putChar(fcontents.charAt(i));
        }
        byte[] recv = send(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        byte status = contents.get();

    }

    public String getfile(String rname) throws IOException {
        byte[] receiveData = new byte[1024];

        ByteBuffer b = ByteBuffer.allocate(1 + 4 + rname.length() * (Character.SIZE >> 3));
        b.put((byte) 13);
        b.putInt(rname.length());
        for (int i = 0; i < rname.length(); i++) {
            b.putChar(rname.charAt(i));
        }

        String str = sendget(b, receiveData);
        return str;

    }

    public String getval(int key) throws IOException {
        byte[] receiveData = new byte[1024];

        ByteBuffer b = ByteBuffer.allocate(1 + (Integer.SIZE >> 3));
        b.put((byte) 10);
        b.putInt(key);

        String str = sendget(b, receiveData);
        return str;

    }

    public int login(String uname, String pcode) throws IOException, CollabConnException {

        byte[] receiveData = new byte[4];

        ByteBuffer b = ByteBuffer.allocate(1 + (Integer.SIZE >> 3) + (uname.length() * (Character.SIZE >> 3)));
        // + Integer.SIZE + (pcode.length() * (Character.SIZE >> 3)));
        b.put((byte) CollabServer.LOGIN);
        b.putInt(uname.length());
        for (int i = 0; i < uname.length(); i++) {
            b.putChar(uname.charAt(i));
        }
        /*b.putInt(pcode.length());
         for (int i = 0; i < pcode.length(); i++) {
         b.putChar(pcode.charAt(i));
         }*/

        byte[] recv = send(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        int sessionid = contents.getInt();
        return sessionid;

    }

    public ArrayList<Integer> searchmain(String uname, int min, int max) throws IOException, CollabConnException {

        byte[] receiveData = new byte[4];

        ByteBuffer b = ByteBuffer.allocate(1 + (Integer.SIZE >> 3) + (uname.length() * (Character.SIZE >> 3))
         + (Integer.SIZE >> 3) + (Integer.SIZE >> 3));
        b.put((byte) CollabServer.SEARCHMAIN);
        b.putInt(uname.length());
        for (int i = 0; i < uname.length(); i++) {
            b.putChar(uname.charAt(i));
        }
        b.putInt(min);
        b.putInt(max);

        ArrayList<Integer> vals = new ArrayList<Integer>();
        sendget(b, receiveData, vals);

        return vals;

    }

    void initsbox(int sessionid) throws IOException, CollabConnException {

        byte[] receiveData = new byte[4];

        ByteBuffer b = ByteBuffer.allocate(1 + Integer.SIZE);
        b.put((byte) CollabServer.INITSANDBOX);
        b.putInt(sessionid);

        byte[] recv = send(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        int fstatus = contents.getInt();
        System.out.println("status:" + fstatus);

    }

    void commit(int sessionid) throws IOException, CollabConnException {
        byte[] receiveData = new byte[4];

        ByteBuffer b = ByteBuffer.allocate(1 + Integer.SIZE);
        b.put((byte) CollabServer.COMMITSANDBOX);
        b.putInt(sessionid);

        byte[] recv = send(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        int fstatus = contents.getInt();
        System.out.println("status:" + fstatus);
    
    }
    
    void rollback(int sessionid) throws IOException, CollabConnException {
        byte[] receiveData = new byte[4];

        ByteBuffer b = ByteBuffer.allocate(1 + Integer.SIZE);
        b.put((byte) CollabServer.DESTROYSANDBOX);
        b.putInt(sessionid);

        byte[] recv = send(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        int fstatus = contents.getInt();
        System.out.println("status:" + fstatus);
    
    }

}
