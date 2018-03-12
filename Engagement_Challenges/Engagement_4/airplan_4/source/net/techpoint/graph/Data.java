package net.techpoint.graph;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

public interface Data {

    String obtain(String key);
    void place(String key, String value);
    void delete(String key);
    boolean containsKey(String key);
    boolean hasData();
    Set<String> keyAssign();
    int size();
    Data copy();
    Element formXMLElement(Document dom);
}
