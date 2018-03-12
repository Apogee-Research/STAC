/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infotrader.datamodel;

import org.w3c.dom.Element;

/**
 *
 serialize(node, state):
	if node is a hyperlink:    
        * // Determined using reflection 		
        * if node links to a Directory other than empty string (which is the name of the root dir) ||
		   node links to a document that is not present on the server:
			return;    // Don’t serialize a bad hyperlink
		serialize_hyperlink(node, state); 
                * // Missing return after this line allows fall through to next if stmt
	if node has the name of a directory           
        * // This check doesn’t omit the empty string.  As such, it allows
		serialize_directory(node, state); 
                * // the hyperlink with empty string name, but no other HLs since
                // the only other ones that could reach here are links to
                // Documents and a Document cannot have the same name as
                // a Directory.
	if node is a document // Determined using reflection, not name
		serialize_document(node, state);
	return;
	
serialize_directory(dir, state):
	write out dir;
for each child c of dir, left to right:
	serialize(c, state) under where dir was written;

serialize_document(doc, state):
	write out doc
	_state = state
	for each child c of doc, left to right: // c is a hyperlink
		if c is not in state: 
			_state += c // then add it to the state
			serialize(c, _state) under where doc was written // and serialize it

serialize_hyperlink(link, state):
	write out link
 */

public interface Node {

    public Element serialize(Node node, SerializationPosition state);

    public String getName();
    
}
