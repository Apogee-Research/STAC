package edu.networkcusp.jackson.simple;

import java.io.IOException;
import java.io.Writer;

/**
 * Beans that support customized output of JSON text to a writer shall implement this interface.  
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public interface JACKSONStreamAware {
	/**
	 * write JSON string to out.
	 */
	void writeJACKSONString(Writer out) throws IOException;
}
