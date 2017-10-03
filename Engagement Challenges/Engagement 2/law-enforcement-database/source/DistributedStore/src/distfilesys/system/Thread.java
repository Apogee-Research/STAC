package distfilesys.system;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import distfilesys.system.dto.DistributedFileDTO;
import distfilesys.system.dto.FileListDTO;
import distfilesys.system.dto.FileStreamDTO;
import distfilesys.system.dto.LockDTO;
import distfilesys.system.dto.PriorityDTO;
import distfilesys.system.exceptions.FailToLockException;
import distfilesys.system.exceptions.FailToUnlockException;

import distfilesys.util.NoSuchElementException;
import io.netty.channel.socket.DatagramPacket;

public class Thread extends java.lang.Thread {

    private Pool<Socket> pool;

    private Message message;

    @SuppressWarnings("unchecked")
    public Thread(Pool pool) {
        this.pool = pool;
    }

    public void run() {

        while (true) {
            try {
                Socket socket = null;
                while (true) {
                    try {

                        socket = pool.get();

                        break;
                    } catch (NoSuchElementException n) {
                        synchronized (this) {
                            wait();
                        }
                    }
                }
                ObjectInputStream in = new ObjectInputStream(socket
                        .getInputStream());

                message = (Message) in.readObject();

                switch (message.getType()) {
                    case Message.HEARTBEAT:
                        heartbeat(socket);
                        break;
                    case Message.LOCK:
                        lock(socket);
                        break;
                    case Message.UNLOCK:
                        unlock(socket);
                        break;
                    case Message.PRIORITY:
                        priority(socket);
                        break;
                    case Message.FILE_STREAM:
                        fileStream(in, socket);
                        break;
                    case Message.FILE_GET:
                        fileOutStream(in, socket);
                        break;
                    case Message.PERM_CHECK:
                        filePermisions(in, socket);
                        break;
                }
                socket.close();
            } catch (Exception e) {
                Logger.severe("Thread error.", e);
            }
        }
    }

    protected void heartbeat(Socket socket) throws IOException {
        Message response = new Message();
        DistributedFileDTO dto = (DistributedFileDTO) message.getData();
        DSystem system = DSystem.getInstance();
        if (system.contains(dto.getDistributedFile())) {
            response.setType(Message.HEARTBEAT);
            response.setData(null);
            new ObjectOutputStream(socket.getOutputStream())
                    .writeObject(response);

            DistributedFile file = dto.getDistributedFile();
            DistributedFile local = system.get(dto.getDistributedFile());
            for (int i = 1; i < 4; i++) {
                Host host = local.getHost(i);
                Host remote = file.getHost(i);
                if (host != null) {
                    if (host.equals(remote)) {
                        host.updateTime();
                    } else {
                        local.setHost(i, remote);
                    }
                } else if (remote != null) {
                    local.setHost(i, remote);
                }
            }

        } else {
            response.setType(Message.ERROR);
            response.setData(null);
            new ObjectOutputStream(socket.getOutputStream())
                    .writeObject(response);
        }

    }

    protected void lock(Socket socket) throws IOException {
        Message response = new Message();
        LockDTO dto = (LockDTO) message.getData();
        DSystem system = DSystem.getInstance();
        DistributedFile file = system.get(dto.getDistributedFile());
        try {
            file.localLock(dto.getHost());
            response.setType(Message.LOCK);
            response.setData(null);
        } catch (FailToLockException e) {
            response.setType(Message.UNLOCK);
            response.setData(null);
        } catch (Exception e) {
            response.setType(Message.ERROR);
            response.setData(null);
        }
        new ObjectOutputStream(socket.getOutputStream()).writeObject(response);
    }

    protected void unlock(Socket socket) throws IOException {
        Message response = new Message();
        LockDTO dto = (LockDTO) message.getData();
        DSystem system = DSystem.getInstance();
        DistributedFile file = system.get(dto.getDistributedFile());
        try {
            file.localUnlock(dto.getHost());
            response.setType(Message.UNLOCK);
            response.setData(null);
        } catch (FailToUnlockException e) {
            response.setType(Message.LOCK);
            response.setData(null);
        } catch (Exception e) {
            response.setType(Message.ERROR);
            response.setData(null);
        }
        new ObjectOutputStream(socket.getOutputStream()).writeObject(response);
    }

    protected void priority(Socket socket) throws IOException {
        Message response = new Message();
        response.setType(Message.PRIORITY);
        double valor = DSystem.getInstance().getHostPriority();
        PriorityDTO dto = new PriorityDTO(valor);
        response.setData(dto);
        new ObjectOutputStream(socket.getOutputStream()).writeObject(response);
    }

    protected void fileStream(ObjectInputStream in, Socket socket)
            throws IOException {
        FileStreamDTO dto = (FileStreamDTO) message.getData();
        System.out.println("receiving:" + dto.getDistributedFile().getName());
        if (dto.getDistributedFile().getName().contains("info")) {
            //socket.close();
            byte bytes[] = new byte[10240];
            int tam = in.read(bytes);
            while (tam != -1) {
                tam = in.read(bytes);
            }
            in.close();
            return;
        }
        File file = new File(DSystem.ROOT + dto.getDistributedFile().getPath());
        FileOutputStream out = new FileOutputStream(file);

        byte bytes[] = new byte[10240];
        int tam = in.read(bytes);
        while (tam != -1) {
            out.write(bytes, 0, tam);
            tam = in.read(bytes);
            
        }
//try{
        ;
        PrintWriter pout = new PrintWriter(socket
                .getOutputStream());
        pout.close();
        in.close();
//} catch(java.net.SocketException jne){
  //          StackTraceElement[] stack = jne.getStackTrace();

            
//}
        
        //pout.print("hjhkjhkhjkkh");
        /*ByteArrayInputStream bs = new ByteArrayInputStream(contentInBytes);
         byte bytes[] = new byte[10240];
         int tam = bs.read(bytes);
         while (tam != -1) {
         out.write(bytes, 0, tam);
         tam = bs.read(bytes);
         }*/
        //pout.flush();

        DistributedFile f = dto.getDistributedFile();
        int host = dto.getHost();
        DSystem.getInstance().setDistributedFile(host, f);
        f.setHost(host, new Host(InetAddress.getByName(DSystem.ADDRESS)
                .getHostAddress()));
        for (int i = 1; i < 4; i++) {
            if (f.getHost(i) != null) {
                f.getHost(i).updateTime();
            }
        }

        out.flush();
        out.close();

        Logger.info("Received file: " + f);
    }

    protected void filePermisions(ObjectInputStream in, Socket socket)
            throws IOException {
        FileListDTO dto = (FileListDTO) message.getData();

        StringBuffer returnval = new StringBuffer();
        String[] list = dto.fileList();
        for (int ind = 0; ind < list.length; ind++) {
            DistributedFile dftemo = new DistributedFile(list[ind]);
            DistributedFile df = DSystem.getInstance().get(dftemo);
            String retrievedcontents = "null";
            if (df != null) {
                retrievedcontents = df.getContents();
            }
            if (retrievedcontents != null) {
                int indexOf = retrievedcontents.indexOf("permoverride=");
                if (indexOf > -1) {
                    String permoverride = retrievedcontents.substring(indexOf, retrievedcontents.length());
                    if (permoverride.equals("1")) {
                        returnval.append("1");
                        continue;
                    }
                }
            }
            returnval.append("0");
        }

                //byte[] contentInBytes = contents.getBytes();
        //ObjectOutputStream out = new ObjectOutputStream(socket
        //      .getOutputStream());
        PrintWriter out = new PrintWriter(socket
                .getOutputStream());
        out.print(returnval);
        //ByteArrayInputStream bs = new ByteArrayInputStream(contentInBytes);
        //byte bytes[] = new byte[10240];
        //int tam = bs.read(bytes);
        //while (tam != -1) {
        //    out.write(bytes, 0, tam);
        //    tam = bs.read(bytes);
        //}
        out.flush();
        //in.close();
        socket.close();

    }

    protected void fileOutStream(ObjectInputStream in, Socket socket)
            throws IOException {
        FileStreamDTO dto = (FileStreamDTO) message.getData();

        DistributedFile f = dto.getDistributedFile();

        //System.out.println(f.file.getName());
        DistributedFile get = DSystem.getInstance().get(f);
        String contents = "null";

        if (get != null) {
            contents = get.getContents();
        }

                //byte[] contentInBytes = contents.getBytes();
        //ObjectOutputStream out = new ObjectOutputStream(socket
        //      .getOutputStream());
        PrintWriter out = new PrintWriter(socket
                .getOutputStream());
        out.print(contents);
        /*ByteArrayInputStream bs = new ByteArrayInputStream(contentInBytes);
         byte bytes[] = new byte[10240];
         int tam = bs.read(bytes);
         while (tam != -1) {
         out.write(bytes, 0, tam);
         tam = bs.read(bytes);
         }*/
        out.flush();
        //in.close();
        socket.close();

        //Logger.info("Received file: " + f);
    }
}
