package net.cybertip.scheme;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

public interface Data {

    String pull(String key);
    void place(String key, String value);
    void delete(String key);
    boolean containsKey(String key);
    boolean hasData();
    Set<String> keySet();
    int size();
    Data copy();
    Element makeXMLElement(Document dom);
}
