/*
 * DistributedFile.java
 *
 * Created on 28 de Abril de 2007, 21:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package distfilesys.system;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import distfilesys.system.dto.FileStreamDTO;
import distfilesys.system.dto.LockDTO;
import distfilesys.system.exceptions.FailToLockException;
import distfilesys.system.exceptions.FailToUnlockException;
import java.io.FileOutputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;

public class DistributedFile implements Serializable {

    private static final long serialVersionUID = 3759327120753282994L;

    public static final int ERROR_LIMIT = 3;

    protected File file;

    /**
     * Armazena os hosts (0-2, mapeado para 1-3) da arquitetura do distfilesys
     */
    protected Host hosts[];

    /**
     * Hora do arquivo no distfilesys.
     */
    protected Date systemTime;

    protected boolean locked;

    protected int lockedHost;

    public DistributedFile(String pathname) {
        file = new File(DSystem.ROOT + pathname);
        hosts = new Host[3];
        systemTime = new Date();
        locked = false;
    }

    public String getContents() throws FileNotFoundException, IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        StringBuffer stringBuffer = new StringBuffer();
        String line = null;

        while ((line = bufferedReader.readLine()) != null) {

            stringBuffer.append(line).append("\n");
        }

        return stringBuffer.toString();
    }

    public void setContents(String contents) throws IOException {

        FileOutputStream out = new FileOutputStream(file);

        StringReader in =new StringReader(contents);

        byte bytes[] = contents.getBytes();

        out.write(bytes, 0, bytes.length);
        
        out.close();
    }

    public String getName() {
        return file.getName();
    }

    public String getPath() {
        return file.getPath().substring(DSystem.ROOT.length());
    }

    public void setHost(int number, Host host) {
        if (host == null) {
            hosts[number - 1] = null;
        } else {
            try {
                hosts[number - 1] = new Host(host.getHostAddress());
            } catch (Exception e) {
                Logger.severe("Host error.", e);
            }
        }
    }

    public void setHost(int number, String hostName) {
        try {
            setHost(number, new Host(hostName));
        } catch (UnknownHostException e) {
            setHost(number, (Host) null);
        }
    }

    public Host getHost(int number) {
        return hosts[number - 1];
    }

    public int getLocalHost() {
        for (int i = 0; i < 3; i++) {
            if (hosts[i] != null
                    && hosts[i].getHostAddress().equalsIgnoreCase(
                            DSystem.ADDRESS)) {
                return i + 1;
            }
        }
        return 0;
    }

    public long getSystemTime() {
        return (systemTime.getTime());
    }

    public void setSystemTime(long time) {
        this.systemTime = new Date(time);
    }

    public long getLocalTime() {
        return file.lastModified();
    }

    public synchronized boolean isLocked() {
        return (locked);
    }

    protected synchronized void localLock(int host) throws FailToLockException {
        if (locked) {
            throw new FailToLockException();
        }
        lockedHost = host;
        locked = true;
    }

    protected synchronized void localUnlock(int host)
            throws FailToUnlockException {
        if (locked && host != lockedHost) {
            throw new FailToUnlockException();
        }
        locked = false;
    }

    public synchronized void lock() throws FailToLockException {
        for (int i = 0; i < hosts.length; i++) {
            if (hosts[i] != null
                    && hosts[i].getHostAddress().equalsIgnoreCase(
                            DSystem.ADDRESS)) {
                localLock(i + 1);
            } else {
                Message lock = new Message();
                Socket socket;
                try {
                    socket = new Socket(hosts[i].getHostAddress(), DSystem.PORT);
                    lock.setType(Message.LOCK);
                    lock.setData(new LockDTO(this));
                    new ObjectOutputStream(socket.getOutputStream())
                            .writeObject(lock);
                    lock = (Message) new ObjectInputStream(socket
                            .getInputStream()).readObject();
                    socket.close();

                    if (lock.getType() != Message.LOCK) {
                        try {
                            if (lock.getType() == Message.ERROR) {
                                unlock();
                            }
                        } catch (Exception e) {
                        }
                        throw new FailToLockException();
                    }
                } catch (UnknownHostException e) {
                    hosts[i] = null;
                } catch (IOException e) {
                    hosts[i] = null;
                } catch (ClassNotFoundException e) {
                } catch (NullPointerException e) {
                } catch (FailToLockException e) {
                    throw new FailToLockException();
                } catch (Exception e) {
                    Logger.warning(this + " lock error: "
                            + e.getLocalizedMessage());
                }

            }
        }
    }

    public synchronized void unlock() throws FailToUnlockException {
        for (int i = 0; i < hosts.length; i++) {
            if (hosts[i] != null
                    && hosts[i].getHostAddress().equalsIgnoreCase(
                            DSystem.ADDRESS)) {
                localUnlock(i + 1);
            } else {
                Message lock = new Message();
                try {
                    Socket socket = new Socket(hosts[i].getHostAddress(),
                            DSystem.PORT);
                    lock.setType(Message.UNLOCK);
                    lock.setData(new LockDTO(this));
                    new ObjectOutputStream(socket.getOutputStream())
                            .writeObject(lock);
                    lock = (Message) new ObjectInputStream(socket
                            .getInputStream()).readObject();
                    socket.close();

                    if (lock.getType() != Message.UNLOCK) {
                        throw new FailToUnlockException();
                    }
                } catch (UnknownHostException e) {
                    hosts[i] = null;
                } catch (IOException e) {
                    hosts[i] = null;
                } catch (ClassNotFoundException e) {
                } catch (NullPointerException e) {
                } catch (FailToUnlockException e) {
                    throw new FailToUnlockException();
                } catch (Exception e) {
                    Logger.warning(this + " unlock error: "
                            + e.getLocalizedMessage());
                }
            }
        }
    }

    public synchronized void election(int host) {
        if (getLocalHost() == 2 && host == 3) {
            return;
        }
        if (getHost(host) != null
                && getHost(host).getErrors() < DistributedFile.ERROR_LIMIT) {
            getHost(host).addError();
        } else {
            new HostElection(this, host);
        }
    }

    @Override
    public int hashCode() {
        return file.getPath().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    public String toString() {
        return getPath();
    }
}
