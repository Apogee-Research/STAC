package distfilesys.system.dto;

import distfilesys.system.DistributedFile;

public class LockDTO implements DTO {

	private static final long serialVersionUID = 1261743754653728459L;

	protected DistributedFile distributedFile;

	protected int host;

	public LockDTO(DistributedFile file) {
		setDistributedFile(file);
		setHost(file.getLocalHost());
	}

	public DistributedFile getDistributedFile() {
		return distributedFile;
	}

	public void setDistributedFile(DistributedFile distributedFile) {
		this.distributedFile = distributedFile;
	}

	public int getHost() {
		return host;
	}

	public void setHost(int host) {
		this.host = host;
	}

}
