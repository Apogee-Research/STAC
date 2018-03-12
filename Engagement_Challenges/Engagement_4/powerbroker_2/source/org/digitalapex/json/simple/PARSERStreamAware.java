package org.digitalapex.json.simple;

import java.io.IOException;
import java.io.Writer;

/**
 * Beans that support customized output of JSON text to a writer shall implement this interface.  
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public interface PARSERStreamAware {
	/**
	 * write JSON string to out.
	 */
	void writePARSERString(Writer out) throws IOException;
}
