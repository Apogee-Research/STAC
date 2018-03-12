package com.roboticcusp.mapping;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

public interface Data {

    String grab(String key);
    void put(String key, String value);
    void delete(String key);
    boolean containsKey(String key);
    boolean hasData();
    Set<String> keyDefine();
    int size();
    Data copy();
    Element composeXMLElement(Document dom);
}
