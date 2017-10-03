package dstore.client;

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

    public static final int PORT = 7689;//Integer.parseInt(System.getProperty("port", default_port));
    static HashMap<Integer, Long> out_Times = new HashMap<Integer, Long>();

    public static HashMap<Integer, Long> finish_times = new HashMap<Integer, Long>();
    public static HashMap<Integer, Boolean> node_split = new HashMap<Integer, Boolean>();
    public static HashMap<Integer, Boolean> beenUsed = new HashMap<Integer, Boolean>();

    /*public static void mainx(String[] args) {
     try {
     String serverHostname = new String("127.0.0.1");
     DatagramSocket clientSocket = new DatagramSocket();
     InetAddress IPAddress = InetAddress.getByName(serverHostname);

     byte[] receiveData = new byte[5];
     Random R = new Random(System.currentTimeMillis());

     for (int i = 10000000; i < 10002000; i++) {
     int r = i;//getUnused(R);
     // Pack random number into byte array and then packet
     ByteBuffer b = ByteBuffer.allocate(6);
     b.put((byte) 1);
     b.putInt(r);
     DatagramPacket sendPacket = new DatagramPacket(b.array(), 5, IPAddress, PORT);
     // Send packet
     clientSocket.send(sendPacket);
     // Record transmission time
     long sent_time = System.nanoTime();
     out_Times.put(r, sent_time);
     // Start waiting for recv
     DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
     clientSocket.setSoTimeout(10000);
     clientSocket.receive(receivePacket);
     // Recv has happened here, log it before parsing the packet
     long recv_time = System.nanoTime();
     byte[] recv = receivePacket.getData();
     ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
     byte status = contents.get();
     int key = r;//contents.getInt();				
     System.out.println(key + "," + status + " recv_time:" + (recv_time - sent_time));
     node_split.put(key, ((int) status == 0) ? true : false);
     finish_times.put(key, recv_time);
     }
     Vector<Long> splits = new Vector<Long>();
     Vector<Long> nonsplits = new Vector<Long>();
     for (Integer item : out_Times.keySet()) {
     if (finish_times.containsKey(item)) {
     if (node_split.containsKey(item)) {
     boolean x = node_split.get(item);
     if (!x) {
     splits.add(finish_times.get(item) - out_Times.get(item));
     } else {
     nonsplits.add(finish_times.get(item) - out_Times.get(item));
     }
     }
     }
     }
     double splits_avg = calculateAverage(splits);
     double nonsplits_avg = calculateAverage(nonsplits);
     System.out.println("splits:" + splits_avg + " size:" + splits.size());
     System.out.println("nonsplits:" + nonsplits_avg + " size:" + nonsplits.size());
     System.out.println(splits_avg - nonsplits_avg);

     clientSocket.close();
     } catch (Exception ex) {
     System.err.println(ex);
     }
     }*/

    /*private static double calculateAverage(List<Long> marks) {
     if (marks == null || marks.size() == 0) {
     return 0;
     }
     if (marks.size() == 1) {
     return marks.get(0);
     }
     double cur_avg = marks.get(0);
     for (int i = 1; i < marks.size(); i++) {
     double cur_val = (double) marks.get(i);
     double not_avg = (((double) i / (i + 1)) * cur_avg) + cur_val / (i + 1);
     cur_avg = not_avg;
     }
     return cur_avg;
     }*/

    /*public static int getUnused(Random R) {
     while (true) {
     int r = R.nextInt(1000000);
     if (!beenUsed.containsKey(r)) {
     beenUsed.put(r, true);
     return r;
     }
     }

     }*/
    public DStoreClient() {

    }

    private DatagramSocket clientSocket;
    private InetAddress IPAddress;

    public void connect(String ipAddress, int port) throws SocketException, UnknownHostException {

        String serverHostname = new String(ipAddress);
        clientSocket = new DatagramSocket();
        IPAddress = InetAddress.getByName(serverHostname);

    }

    public byte insertnewkey(int key) throws IOException {
        byte[] receiveData = new byte[5];

        // Pack random number into byte array and then packet
        ByteBuffer b = ByteBuffer.allocate(5);
        b.put((byte) 1);
        b.putInt(key);

        byte[] recv = send(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        byte status = contents.get();
        return status;
    }

    public byte delete(int key) throws IOException {
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

    

    public void update(int key, String rname) throws IOException {
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

    private byte[] send(ByteBuffer b, byte[] receiveData) throws IOException {

        byte[] array = b.array();
        DatagramPacket sendPacket = new DatagramPacket(array, array.length, IPAddress, PORT);
        // Send packet
        clientSocket.send(sendPacket);
        // Start waiting for recv
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        // Recv has happened here
        byte[] recv = receivePacket.getData();
        return recv;

    }

    private void sendget(ByteBuffer b, byte[] buf, ArrayList<Integer> vals) throws IOException {

        /*b.position(1);
         int aInt = b.getInt();
         int bInt = b.getInt();*/
        byte[] array = b.array();
        DatagramPacket sendPacket = new DatagramPacket(array, array.length, IPAddress, PORT);
        // Send packet
        clientSocket.send(sendPacket);
        // Start waiting for recv
        /*DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
         clientSocket.receive(receivePacket);
         // Recv has happened here
         byte[] recv = receivePacket.getData();
         return recv;*/
        boolean keepGoing = true;
        while (keepGoing) {
            DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
            clientSocket.receive(receivePacket);

            byte[] data = receivePacket.getData();
            if (data == null || data.length == 0) {
                break;
            }
            ByteBuffer contents = ByteBuffer.wrap(data, 0, data.length);
            int v = contents.getInt();

            //if the packet is empty or null, then the server is done sending?
            if (v == -8) {
                keepGoing = false;
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

    public void beginTransaction() throws IOException {
        byte[] receiveData = new byte[1];

        ByteBuffer b = ByteBuffer.allocate(1);
        b.put((byte) 3);
        byte[] recv = send(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        byte status = contents.get();
    }

    public boolean add(int k, int d, boolean b) throws IOException {

        byte ret = insertnewkey(k);
        //insertdata(k, d);
        return true;
    }

    public void commit() throws IOException {
        byte[] receiveData = new byte[1];

        ByteBuffer b = ByteBuffer.allocate(1);
        b.put((byte) 4);
        byte[] recv = send(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        byte status = contents.get();
    }

    public void rollback() throws IOException {
        byte[] receiveData = new byte[1];

        ByteBuffer b = ByteBuffer.allocate(1);
        b.put((byte) 5);
        byte[] recv = send(b, receiveData);

        ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        byte status = contents.get();
    }

    public void fastInsert(boolean onoff) throws IOException {
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
    public ArrayList<Integer> search(int min, int max) throws IOException {
        byte[] receiveData = new byte[4];

        ByteBuffer b = ByteBuffer.allocate(9);
        b.put((byte) 8);
        b.putInt(min);
        b.putInt(max);

        ArrayList<Integer> vals = new ArrayList<Integer>();
        sendget(b, receiveData, vals);

        return vals;
    }

    public void storefile(String rname, String fcontents) throws IOException {
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

}
