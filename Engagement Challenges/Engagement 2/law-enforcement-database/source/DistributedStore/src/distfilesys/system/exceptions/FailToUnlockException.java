package distfilesys.system.exceptions;

public class FailToUnlockException extends RuntimeException {

	private static final long serialVersionUID = 2541899867176901702L;

	public FailToUnlockException() {
		super("Fail to Unlock Distributed File.");
	}

}
