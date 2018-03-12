package edu.cyberapex.parsing.simple;

/**
 * Beans that support customized output of JSON text shall implement this interface.  
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public interface PARTAware {
	/**
	 * @return JSON text
	 */
	String toPARTString();
}
