package org.digitalapex.json.simple.grabber;

import java.io.IOException;

/**
 * A simplified and stoppable SAX-like content handler for stream processing of JSON text. 
 * 
 * @see org.xml.sax.ContentHandler
 * @see PARSERGrabber#parse(java.io.Reader, ContentCoach, boolean)
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public interface ContentCoach {
	/**
	 * Receive notification of the beginning of JSON processing.
	 * The parser will invoke this method only once.
     * 
	 * @throws ParseRaiser
	 * 			- JSONParser will stop and throw the same exception to the caller when receiving this exception.
	 */
	void startPARSER() throws ParseRaiser, IOException;
	
	/**
	 * Receive notification of the end of JSON processing.
	 * 
	 * @throws ParseRaiser
	 */
	void endPARSER() throws ParseRaiser, IOException;
	
	/**
	 * Receive notification of the beginning of a JSON object.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseRaiser
     *          - JSONParser will stop and throw the same exception to the caller when receiving this exception.
     * @see #endPARSER
	 */
	boolean startObject() throws ParseRaiser, IOException;
	
	/**
	 * Receive notification of the end of a JSON object.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseRaiser
     * 
     * @see #startObject
	 */
	boolean endObject() throws ParseRaiser, IOException;
	
	/**
	 * Receive notification of the beginning of a JSON object entry.
	 * 
	 * @param key - Key of a JSON object entry. 
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseRaiser
     * 
     * @see #endObjectEntry
	 */
	boolean startObjectEntry(String key) throws ParseRaiser, IOException;
	
	/**
	 * Receive notification of the end of the value of previous object entry.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseRaiser
     * 
     * @see #startObjectEntry
	 */
	boolean endObjectEntry() throws ParseRaiser, IOException;
	
	/**
	 * Receive notification of the beginning of a JSON array.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseRaiser
     * 
     * @see #endArray
	 */
	boolean startArray() throws ParseRaiser, IOException;
	
	/**
	 * Receive notification of the end of a JSON array.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseRaiser
     * 
     * @see #startArray
	 */
	boolean endArray() throws ParseRaiser, IOException;
	
	/**
	 * Receive notification of the JSON primitive values:
	 * 	java.lang.String,
	 * 	java.lang.Number,
	 * 	java.lang.Boolean
	 * 	null
	 * 
	 * @param value - Instance of the following:
	 * 			java.lang.String,
	 * 			java.lang.Number,
	 * 			java.lang.Boolean
	 * 			null
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseRaiser
	 */
	boolean primitive(Object value) throws ParseRaiser, IOException;
		
}
