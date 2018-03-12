package com.digitalpoint.jack.simple;

import java.io.IOException;
import java.io.Writer;

/**
 * Beans that support customized output of JSON text to a writer shall implement this interface.  
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public interface OBJNOTEStreamAware {
	/**
	 * write JSON string to out.
	 */
	void writeOBJNOTEString(Writer out) throws IOException;
}
