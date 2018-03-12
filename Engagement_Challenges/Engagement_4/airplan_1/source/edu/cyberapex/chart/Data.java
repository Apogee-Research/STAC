package edu.cyberapex.chart;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

public interface Data {

    String fetch(String key);
    void put(String key, String value);
    void delete(String key);
    boolean containsKey(String key);
    boolean hasData();
    Set<String> keySet();
    int size();
    Data copy();
    Element generateXMLElement(Document dom);
}
