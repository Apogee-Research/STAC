package net.computerpoint.parsing.simple;

/**
 * Beans that support customized output of JSON text shall implement this interface.  
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public interface PARSERAware {
	/**
	 * @return JSON text
	 */
	String toPARSERString();
}
