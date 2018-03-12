package com.virtualpoint.part.simple;

/**
 * Beans that support customized output of JSON text shall implement this interface.  
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public interface PLUGINAware {
	/**
	 * @return JSON text
	 */
	String toPLUGINString();
}
