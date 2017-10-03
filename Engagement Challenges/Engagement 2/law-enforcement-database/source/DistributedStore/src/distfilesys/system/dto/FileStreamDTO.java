package distfilesys.system.dto;

import distfilesys.system.DistributedFile;

public class FileStreamDTO implements DTO {

	private static final long serialVersionUID = 6394144785289614398L;

	protected DistributedFile distributedFile;

	protected int host;

	public FileStreamDTO(DistributedFile distributedFile, int host) {
		setDistributedFile(distributedFile);
		setHost(host);
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
