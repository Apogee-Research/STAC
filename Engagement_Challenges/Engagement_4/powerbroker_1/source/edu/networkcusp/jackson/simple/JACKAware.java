package edu.networkcusp.jackson.simple;

/**
 * Beans that support customized output of JSON text shall implement this interface.  
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public interface JACKAware {
	/**
	 * @return JSON text
	 */
	String toJACKString();
}
