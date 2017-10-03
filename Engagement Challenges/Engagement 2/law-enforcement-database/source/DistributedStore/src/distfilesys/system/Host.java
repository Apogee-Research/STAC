package distfilesys.system;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import distfilesys.system.dto.PriorityDTO;

public class Host implements Serializable {

	private static final long serialVersionUID = -9031423868963684967L;

	protected InetAddress host;

	protected Date updateTime;

	protected int errors;

	public Host(String host) throws UnknownHostException {
		if (host == null)
			throw new UnknownHostException();
		this.host = InetAddress.getByName(host);
		updateTime();
	}

	public String getHostAddress() {
		return host.getHostAddress();
	}

	public String getHostName() {
		return host.getHostName();
	}

	public void updateTime() {
		updateTime = new Date();
		errors = 0;
	}

	public void addError() {
		errors++;
	}

	public int getErrors() {
		return errors;
	}

	public Date getUpdatedTime() {
		return updateTime;
	}

	public double getPriority() {
		double valor = 10;
		try {
			Socket socket = new Socket(host, DSystem.PORT);
			Message priority = new Message();
			priority.setType(Message.PRIORITY);
			new ObjectOutputStream(socket.getOutputStream())
					.writeObject(priority);
			priority = (Message) new ObjectInputStream(socket.getInputStream())
					.readObject();
			socket.close();
			if (priority.getType() == Message.PRIORITY)
				valor = ((PriorityDTO) priority.getData()).getPriority();
		} catch (Exception e) {
			Logger
					.warning("Getting priority error: "
							+ e.getLocalizedMessage());
		}
		return valor;
	}

	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (getHostAddress().equalsIgnoreCase(((Host) o).getHostAddress()))
			return true;
		return false;
	}

	public String toString() {
		if (host == null)
			return null;
		return host.getHostAddress();
	}
}
