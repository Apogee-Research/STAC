package distfilesys.system.exceptions;

public class FailToLockException extends RuntimeException {

	private static final long serialVersionUID = -2018001963389232497L;

	public FailToLockException() {
		super("Fail to Lock Distributed File!");
	}

}
