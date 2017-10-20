package distfilesys.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import distfilesys.system.dto.FileStreamDTO;
import distfilesys.system.exceptions.FailToLockException;
import distfilesys.system.exceptions.FailToUnlockException;

public class HostElection implements Runnable {

	private DistributedFile file;

	private int host;

	public HostElection(DistributedFile file, int host) {
		this.file = file;
		this.host = host;
		new java.lang.Thread(this).start();
	}

	public void run() {
		file.setHost(host, (Host) null);

		try {
			if (file.isLocked())
				return;
			file.lock();
		} catch (FailToLockException e) {
			Logger.warning(file + ": " + e.getMessage());
			return;
		}

		Host eleito = null;
		boolean removed = false;
		while (!removed) {
			List<Host> hosts = DSystem.getInstance().getHosts();
			try {
				if (hosts.size() == 0) {
					Thread.sleep(1000);
					continue;
				}
			} catch (Exception e) {
				Thread.yield();
				continue;
			}

			double valor = Double.POSITIVE_INFINITY;
			for (Host host : hosts) {
				boolean get = true;
				for (int i = 1; i < 4; i++) {
					Host h = file.getHost(i);
					if (h != null && h.equals(host))
						get = false;
				}
				if (!get)
					continue;
				double v = host.getPriority();
				if (v < valor) {
					eleito = host;
					valor = v;
				}

			}
			removed = DSystem.getInstance().removeHost(eleito);
		}

		Logger.info("Election for: " + file + ": " + eleito);

		try {
			File file = new File(DSystem.ROOT + this.file.getPath());
			FileStreamDTO dto = new FileStreamDTO(this.file, host);
			Message msg = new Message();
			msg.setData(dto);
			msg.setType(Message.FILE_STREAM);

			Socket socket = new Socket(eleito.getHostAddress(), DSystem.PORT);
			ObjectOutputStream out = new ObjectOutputStream(socket
					.getOutputStream());

			out.writeObject(msg);

			FileInputStream in = new FileInputStream(file);
			byte buffer[] = new byte[10240];
			int tam;
			tam = in.read(buffer);
			while (tam != -1) {
				out.write(buffer, 0, tam);
				tam = in.read(buffer);
			}
			out.flush();
			socket.close();

			this.file.setHost(host, eleito);
			try {
				Thread.sleep(500);
				this.file.unlock();
			} catch (FailToUnlockException e) {
				Logger.warning(this.file + ": " + e.getMessage());
			}
		} catch (Exception e) {
			Logger.severe("Election error.", e);
		}
		DSystem.getInstance().addHost(eleito);
	}
}
