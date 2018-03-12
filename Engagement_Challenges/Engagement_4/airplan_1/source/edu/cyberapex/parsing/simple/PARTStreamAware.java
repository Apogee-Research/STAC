package edu.cyberapex.parsing.simple;

import java.io.IOException;
import java.io.Writer;

/**
 * Beans that support customized output of JSON text to a writer shall implement this interface.  
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public interface PARTStreamAware {
	/**
	 * write JSON string to out.
	 */
	void writePARTString(Writer out) throws IOException;
}
