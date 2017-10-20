package distfilesys.system;

import static java.lang.Math.abs;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.Random;

import distfilesys.system.dto.DistributedFileDTO;

/**
 * @author Calebe de Paula Bianchini
 * 
 */
public class HeartBeat extends java.lang.Thread {

	private static final long TIME = 15000;

	private int hostNumber;


	public HeartBeat(int hostNumber) {
		this.hostNumber = hostNumber;
		start();
	}

	public void run() {
		/*if (hostNumber == 3)
			onlyWaitHeartBeats();
		runHeartBeat();*/
	}

	/*protected void waitHeartBeat() {
		DSystem system = DSystem.getInstance();
		List<DistributedFile> files;
		files = system.getList(hostNumber);
		long date;
		for (DistributedFile file : files) {
			int election = -1;
			for (int host = 1; host < hostNumber; host++) {
				date = new Date().getTime();
				if (!(file.getHost(host) == null || abs(date
						- file.getHost(host).getUpdatedTime().getTime()) > TIME)) {
					election = -1;
					break;
				}
				election = host;
			}
			if (election != -1) {
				file.election(election);
				break;
			}
		}
	}

	protected void onlyWaitHeartBeats() {
		long time = abs((new Random(new Date().getTime()).nextLong() % (TIME / 2)))
				+ (TIME / 2);
		while (true) {
			try {
				sleep(time);
				waitHeartBeat();
			} catch (Exception e) {
				Logger.severe("Heartbeat error.", e);
			}
			time = TIME;
		}
	}*/

	/*protected void runHeartBeat() {
		long time = abs((new Random(new Date().getTime()).nextLong() % (TIME / 2)))
				+ (TIME / 2);
		DSystem system = DSystem.getInstance();
		while (true) {
			try {
				sleep(time);

				int counts = 3 - hostNumber;

				List<DistributedFile> files;
				files = system.getList(hostNumber);
				for (DistributedFile file : files) {
					for (int j = hostNumber + 1; j <= hostNumber + counts; j++) {
						Message hb = new Message();
						try {
							Socket socket = new Socket(file.getHost(j)
									.getHostAddress(), DSystem.PORT);

							hb.setType(Message.HEARTBEAT);
							hb.setData(new DistributedFileDTO(file));

							new ObjectOutputStream(socket.getOutputStream())
									.writeObject(hb);

							hb = (Message) new ObjectInputStream(socket
									.getInputStream()).readObject();
							socket.close();
						} catch (Exception e) {
							hb.setType(Message.ERROR);
						}

						if (hb.getType() == Message.HEARTBEAT) {
							file.getHost(j).updateTime();
						} else {
							file.election(j);
						}
					}
				}
				if (hostNumber == 2)
					waitHeartBeat();
			} catch (Exception e) {
				Logger.severe("Heartbeat error.", e);
			}
			time = TIME;
		}
	}*/
}
