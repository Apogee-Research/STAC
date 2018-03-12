package edu.cyberapex.parsing.simple.extractor;

import java.io.IOException;

/**
 * A simplified and stoppable SAX-like content handler for stream processing of JSON text. 
 * 
 * @see org.xml.sax.ContentHandler
 * @see PARTReader#parse(java.io.Reader, ContentGuide, boolean)
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public interface ContentGuide {
	/**
	 * Receive notification of the beginning of JSON processing.
	 * The parser will invoke this method only once.
     * 
	 * @throws ParseFailure
	 * 			- JSONParser will stop and throw the same exception to the caller when receiving this exception.
	 */
	void startPART() throws ParseFailure, IOException;
	
	/**
	 * Receive notification of the end of JSON processing.
	 * 
	 * @throws ParseFailure
	 */
	void endPART() throws ParseFailure, IOException;
	
	/**
	 * Receive notification of the beginning of a JSON object.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseFailure
     *          - JSONParser will stop and throw the same exception to the caller when receiving this exception.
     * @see #endPART
	 */
	boolean startObject() throws ParseFailure, IOException;
	
	/**
	 * Receive notification of the end of a JSON object.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseFailure
     * 
     * @see #startObject
	 */
	boolean endObject() throws ParseFailure, IOException;
	
	/**
	 * Receive notification of the beginning of a JSON object entry.
	 * 
	 * @param key - Key of a JSON object entry. 
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseFailure
     * 
     * @see #endObjectEntry
	 */
	boolean startObjectEntry(String key) throws ParseFailure, IOException;
	
	/**
	 * Receive notification of the end of the value of previous object entry.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseFailure
     * 
     * @see #startObjectEntry
	 */
	boolean endObjectEntry() throws ParseFailure, IOException;
	
	/**
	 * Receive notification of the beginning of a JSON array.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseFailure
     * 
     * @see #endArray
	 */
	boolean startArray() throws ParseFailure, IOException;
	
	/**
	 * Receive notification of the end of a JSON array.
	 * 
	 * @return false if the handler wants to stop parsing after return.
	 * @throws ParseFailure
     * 
     * @see #startArray
	 */
	boolean endArray() throws ParseFailure, IOException;
	
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
	 * @throws ParseFailure
	 */
	boolean primitive(Object value) throws ParseFailure, IOException;
		
}
