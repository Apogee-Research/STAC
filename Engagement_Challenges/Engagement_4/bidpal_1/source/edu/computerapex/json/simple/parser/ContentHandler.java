package edu.computerapex.json.simple.parser;

import java.io.IOException;

/**
 * A simplified and stoppable SAX-like content handler for stream processing of JSON text. 
 * 
 * @see org.xml.sax.ContentHandler
 * @see JSONRetriever#parse(java.io.Reader, ContentHandler, boolean)
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public interface ContentHandler {
	/**
	 * Receive notification of the beginning of JSON processing.
	 * The parser will invoke this method only once.
     * 
	 * @throws ParseDeviation
	 * 			- JSONParser will stop and throw the same exception to the caller when receiving this exception.
	 */
	void startJSON() throws ParseDeviation, IOException;
	
	/**
	 * Receive notification of the end of JSON processing.
	 * 
	 * @throws ParseDeviation
	 */
	void endJSON() throws ParseDeviation, IOException;
	
	/**
	 * Receive notification of the beginning of a JSON object.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseDeviation
     *          - JSONParser will stop and throw the same exception to the caller when receiving this exception.
     * @see #endJSON
	 */
	boolean startObject() throws ParseDeviation, IOException;
	
	/**
	 * Receive notification of the end of a JSON object.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseDeviation
     * 
     * @see #startObject
	 */
	boolean endObject() throws ParseDeviation, IOException;
	
	/**
	 * Receive notification of the beginning of a JSON object entry.
	 * 
	 * @param key - Key of a JSON object entry. 
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseDeviation
     * 
     * @see #endObjectEntry
	 */
	boolean startObjectEntry(String key) throws ParseDeviation, IOException;
	
	/**
	 * Receive notification of the end of the value of previous object entry.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseDeviation
     * 
     * @see #startObjectEntry
	 */
	boolean endObjectEntry() throws ParseDeviation, IOException;
	
	/**
	 * Receive notification of the beginning of a JSON array.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseDeviation
     * 
     * @see #endArray
	 */
	boolean startArray() throws ParseDeviation, IOException;
	
	/**
	 * Receive notification of the end of a JSON array.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseDeviation
     * 
     * @see #startArray
	 */
	boolean endArray() throws ParseDeviation, IOException;
	
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
	 * @throws ParseDeviation
	 */
	boolean primitive(Object value) throws ParseDeviation, IOException;
		
}
