package distfilesys.system;

import java.io.Serializable;
import distfilesys.system.dto.DTO;

public class Message implements Serializable {

	private static final long serialVersionUID = -5762616931827601672L;

	public static final int ERROR = 0;

	public static final int HEARTBEAT = 1;

	public static final int LOCK = 2;

	public static final int UNLOCK = 3;

	public static final int PRIORITY = 4;

	public static final int FILE_STREAM = 5;
    
        public static final int FILE_GET = 6;
        
        public static final int PERM_CHECK = 7;

	private int type = ERROR;

	private DTO data;

	public DTO getData() {
		return data;
	}

	public void setData(DTO data) {
		this.data = data;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
