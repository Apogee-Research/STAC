package distfilesys.system;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import distfilesys.system.DistributedFile;

import distfilesys.system.election.DefaultElection;
import distfilesys.system.election.Election;

public class DSystem extends java.lang.Thread {

	public static final String ROOT = "./files/";

	public static int PORT = 6666;

	public static String ADDRESS = "127.0.0.1";

	private Pool<Socket> pool;

	/**
	 * Cada indice (0-2, mapeado para 1-3) indica um conjunto de arquivos. Cada
	 * conjunto representa o <i>host</i> local na arquitetura do distfilesys.
	 */
	private HashSet<String> listas[];

	private ArrayList<Host> hosts;

	private Election election;

	private static DSystem singletonSystem = null;

	public static synchronized DSystem getInstance() {
		if (singletonSystem == null)
			singletonSystem = new DSystem();
		return singletonSystem;
	}

	public static void main(String[] args) {
		try {
			DSystem.ADDRESS = "127.0.0.1";
			DSystem system = DSystem.getInstance();
			system.join();
		} catch (Exception e) {
			Logger.severe("System error.", e);
		}
	}

	@SuppressWarnings("unchecked")
	private DSystem() {
		listas = new HashSet[3];
		for (int i = 0; i < listas.length; i++)
			listas[i] = new HashSet<String>();
		pool = new Pool();
		new HeartBeat(1);
		new HeartBeat(2);
		new HeartBeat(3);

		loadHosts();
                loadFiles();
		election = new DefaultElection();

		start();
	}

	public void run() {
		ServerSocket server = null;
		try {

			server = new ServerSocket();
                        //server.setReuseAddress(true);
			server.bind(new InetSocketAddress(DSystem.ADDRESS, DSystem.PORT));
		} catch (Exception e) {
			Logger.severe("Unable to initialize distfilesys System.", e);
			java.lang.System.out
					.println("Unable to initialize distfilesys System.");
			java.lang.System.exit(1);
		}
		Logger.info("distfilesys system initialized.");
		while (true) {
			try {
				pool.put(server.accept());
			} catch (Exception e) {
				Logger.severe("Server socket error: ", e);
			}
		}
	}

	private void loadHosts() {
		hosts = new ArrayList<Host>(30);
		try {
			String local = InetAddress.getByName(DSystem.ADDRESS)
					.getHostAddress();
			Scanner in = new Scanner(new File("hosts"));
			while (in.hasNext()) {
				try {
					Host host = new Host(in.nextLine());
					if (!local.equalsIgnoreCase(host.getHostAddress()))
						hosts.add(host);
				} catch (Exception e) {
				}
			}
			Logger.info("Found " + hosts.size() + " hosts.");
		} catch (Exception e) {
			Logger.warning("No hosts found.");
		}
	}

	public synchronized List<Host> getHosts() {
		return new ArrayList<Host>(hosts);
	}

	public synchronized void addHost(Host host) {
		try {
			hosts.add(new Host(host.getHostAddress()));
		} catch (Exception e) {
			Logger.warning("Host error: " + host.getHostAddress());
		}
	}

	public synchronized boolean removeHost(Host host) {
		return hosts.remove(host);
	}

	public synchronized List<String> getList(int number) {
		return new ArrayList<String>(listas[number - 1]);
	}

	public synchronized void setDistributedFile(int listNumber,
			DistributedFile file) {
		listas[listNumber - 1].add(file.getName());
	}


	public boolean contains(DistributedFile file) {
		for (int i = 0; i < 3; i++)
			if (listas[i].contains(file))
				return true;
		return false;
	}

	public DistributedFile get(DistributedFile file) {
		for (int i = 0; i < 3; i++)
			if (listas[i].contains(file.getName())) {
				List<String> list = getList(i + 1);
                                list.get(list.indexOf(file.getName()));
                                DistributedFile df = new DistributedFile(file.getName());
				return df;
			}
		return null;
	}

	public double getHostPriority() {
		return election.calculate();
	}

    private void loadFiles() {

        File folder = new File(ROOT);
        
            File[] listFiles = folder.listFiles();
            
            for(int i=0;i<listFiles.length;i++){
            
                if(listFiles[i].isFile()){
                    setDistributedFile(1, new DistributedFile(listFiles[i].getName()));
                }
            }
    
    }
}
