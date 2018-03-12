package com.virtualpoint.part.simple.retriever;

import java.io.IOException;

/**
 * A simplified and stoppable SAX-like content handler for stream processing of JSON text. 
 * 
 * @see org.xml.sax.ContentHandler
 * @see PLUGINRetriever#parse(java.io.Reader, ContentCoach, boolean)
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public interface ContentCoach {
	/**
	 * Receive notification of the beginning of JSON processing.
	 * The parser will invoke this method only once.
     * 
	 * @throws ParseTrouble
	 * 			- JSONParser will stop and throw the same exception to the caller when receiving this exception.
	 */
	void startPLUGIN() throws ParseTrouble, IOException;
	
	/**
	 * Receive notification of the end of JSON processing.
	 * 
	 * @throws ParseTrouble
	 */
	void endPLUGIN() throws ParseTrouble, IOException;
	
	/**
	 * Receive notification of the beginning of a JSON object.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseTrouble
     *          - JSONParser will stop and throw the same exception to the caller when receiving this exception.
     * @see #endPLUGIN
	 */
	boolean startObject() throws ParseTrouble, IOException;
	
	/**
	 * Receive notification of the end of a JSON object.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseTrouble
     * 
     * @see #startObject
	 */
	boolean endObject() throws ParseTrouble, IOException;
	
	/**
	 * Receive notification of the beginning of a JSON object entry.
	 * 
	 * @param key - Key of a JSON object entry. 
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseTrouble
     * 
     * @see #endObjectEntry
	 */
	boolean startObjectEntry(String key) throws ParseTrouble, IOException;
	
	/**
	 * Receive notification of the end of the value of previous object entry.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseTrouble
     * 
     * @see #startObjectEntry
	 */
	boolean endObjectEntry() throws ParseTrouble, IOException;
	
	/**
	 * Receive notification of the beginning of a JSON array.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseTrouble
     * 
     * @see #endArray
	 */
	boolean startArray() throws ParseTrouble, IOException;
	
	/**
	 * Receive notification of the end of a JSON array.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseTrouble
     * 
     * @see #startArray
	 */
	boolean endArray() throws ParseTrouble, IOException;
	
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
	 * @throws ParseTrouble
	 */
	boolean primitive(Object value) throws ParseTrouble, IOException;
		
}
