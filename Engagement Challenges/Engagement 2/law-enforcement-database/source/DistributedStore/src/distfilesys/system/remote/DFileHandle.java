/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distfilesys.system.remote;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import distfilesys.system.DSystem;
import distfilesys.system.DistributedFile;
import distfilesys.system.Message;
import distfilesys.system.dto.FileListDTO;
import distfilesys.system.dto.FileStreamDTO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author user
 */
public class DFileHandle {

    String name;
    DSystemHandle syshandle;
    private String contents;

    public DFileHandle(String name, DSystemHandle syshandle) {
        this.name = name;
        this.syshandle = syshandle;

    }

    public void setContents(String contents) {

        this.contents = contents;
    }

    public static void main(String[] args) throws IOException {
        DSystemHandle sys = sys = new DSystemHandle("127.0.0.1", 6666);
        String[] f = {"a", "b", "c"};
        getContents(f, sys);

    }

    public static String getContents(String[] names, DSystemHandle syshandle) throws IOException {

        FileListDTO dto = new FileListDTO(names);

        Message msg = new Message();
        msg.setType(Message.PERM_CHECK);
        msg.setData(dto);

            Socket socket = new Socket();//    DSystem.ADDRESS, DSystem.PORT);
            socket.setReuseAddress(true);
            socket.bind(null);
            socket.connect(new InetSocketAddress(DSystem.ADDRESS, DSystem.PORT));
        //Socket socket = new Socket(DSystem.ADDRESS, DSystem.PORT);
        //socket.setReuseAddress(true);
        ObjectOutputStream out = new ObjectOutputStream(socket
                .getOutputStream());
        out.writeObject(msg);

        out.flush();

        BufferedReader datafromserver = new BufferedReader(
                new InputStreamReader(
                        socket.getInputStream()));

        String fromServer;
        String data = "";
        while ((fromServer = datafromserver.readLine()) != null) {
            //System.out.println("Server: " + fromServer);

            if (fromServer.equals("done.")) {
                break;
            } else {
                data += fromServer;
            }
        }

        datafromserver.close();
        socket.close();

        return fromServer;

    }

    public void store(Socket socket, ObjectOutputStream out) throws IOException {

        DistributedFile file = new DistributedFile(this.name);
        FileStreamDTO dto = new FileStreamDTO(file, 1);
        Message msg = new Message();
        msg.setType(Message.FILE_STREAM);
        msg.setData(dto);

        //long nanoTime = System.nanoTime();
        socket = null;
        if (socket == null) {
            socket = new Socket();//    DSystem.ADDRESS, DSystem.PORT);
            socket.setReuseAddress(true);
            socket.bind(null);
            socket.connect(new InetSocketAddress(DSystem.ADDRESS, DSystem.PORT));
        }
        out = null;
        if (out == null) {
            out = new ObjectOutputStream(socket
                    .getOutputStream());
        }

        //long nanoTime2 = System.nanoTime();
        //System.out.println(nanoTime2-nanoTime);
        //nanoTime = System.nanoTime();
        out.writeObject(msg);
        byte[] contentInBytes = contents.getBytes();

        ByteArrayInputStream in = new ByteArrayInputStream(contentInBytes);
        byte bytes[] = new byte[10240];
        int tam = in.read(bytes);
        while (tam != -1) {
            out.write(bytes, 0, tam);
            tam = in.read(bytes);
        }
        out.flush();
//nanoTime2 = System.nanoTime();

        //System.out.println(nanoTime2-nanoTime);
        //nanoTime = System.nanoTime();
        in.close();
        out.close();
        socket.close();
        //nanoTime2 = System.nanoTime(); 
        //System.out.println(nanoTime2-nanoTime);
        //System.out.println("---");

    }

    public void storefast(Socket socket, ObjectOutputStream out) throws IOException {

        DistributedFile file = new DistributedFile(this.name);
        System.out.println("sending:" + this.name);
        FileStreamDTO dto = new FileStreamDTO(file, 1);
        Message msg = new Message();
        msg.setType(Message.FILE_STREAM);
        msg.setData(dto);

        //long nanoTime = System.nanoTime();
        socket = null;
        if (socket == null) {
            socket = new Socket();//    DSystem.ADDRESS, DSystem.PORT);
            socket.setReuseAddress(true);
            socket.bind(null);
            socket.connect(new InetSocketAddress(DSystem.ADDRESS, DSystem.PORT));
            try {

                FileOutputStream fout = new FileOutputStream(file.getName());

                StringReader fin = new StringReader(file.getContents());
                byte bytes[] = contents.getBytes();

                fout.write(bytes, 0, bytes.length);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        /*if(socket!=null){
         socket.close();
         return;
         }*/
        out = null;
        if (out == null) {
            out = new ObjectOutputStream(socket
                    .getOutputStream());
        }

        //long nanoTime2 = System.nanoTime();
        //System.out.println(nanoTime2-nanoTime);
        //nanoTime = System.nanoTime();
        out.writeObject(msg);
        byte[] contentInBytes = contents.getBytes();

        ByteArrayInputStream in = new ByteArrayInputStream(contentInBytes);
        byte bytes[] = new byte[10240];
        int tam = in.read(bytes);
        while (tam != -1) {
            out.write(bytes, 0, tam);
            tam = in.read(bytes);
        }
        out.flush();
//nanoTime2 = System.nanoTime();

        //System.out.println(nanoTime2-nanoTime);
        //nanoTime = System.nanoTime();
        in.close();
        out.close();
        socket.close();

        try {
            File l = new File(file.getName());

            l.delete();
        } catch (Exception e) {
        }
        //nanoTime2 = System.nanoTime(); 
        //System.out.println(nanoTime2-nanoTime);
        //System.out.println("---");

    }

    public String retrieve() throws IOException {

        DistributedFile file = new DistributedFile(name);
        FileStreamDTO dto = new FileStreamDTO(file, 1);
        Message msg = new Message();
        msg.setType(Message.FILE_GET);
        msg.setData(dto);

        Socket socket = new Socket(DSystem.ADDRESS, DSystem.PORT);
        ObjectOutputStream out = new ObjectOutputStream(socket
                .getOutputStream());
        out.writeObject(msg);

        out.flush();

        BufferedReader datafromserver = new BufferedReader(
                new InputStreamReader(
                        socket.getInputStream()));

        String fromServer;
        String data = "";
        while ((fromServer = datafromserver.readLine()) != null) {
            // System.out.println("Server: " + fromServer);

            if (fromServer.equals("done.")) {
                break;
            } else {
                data += fromServer;
            }
        }

        socket.close();

        return data;
    }

    public static boolean exists() throws IOException {

        Message msg = new Message();
        msg.setType(Message.FILE_STREAM);
        //msg.setData(dto);

        Socket socket = new Socket(DSystem.ADDRESS, DSystem.PORT);
        ObjectOutputStream out = new ObjectOutputStream(socket
                .getOutputStream());
        out.writeObject(msg);

        BufferedReader datafromserver = new BufferedReader(
                new InputStreamReader(
                        socket.getInputStream()));

        String fromServer;
        while ((fromServer = datafromserver.readLine()) != null) {
            //System.out.println("Server: " + fromServer);
            if (fromServer.equals("done.")) {
                break;
            }
        }

        out.flush();
        socket.close();

        return true;
    }

}
