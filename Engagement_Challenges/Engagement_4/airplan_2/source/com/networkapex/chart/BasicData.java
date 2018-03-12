package com.networkapex.chart;

import com.networkapex.sort.DefaultComparator;
import com.networkapex.sort.Orderer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class BasicData implements Data {
    private HashMap<String, String> data;

    public BasicData() {
        data = new HashMap<>();
    }

    public BasicData(int weight) {
        this();

        data.put("weight", Integer.toString(weight));
    }

    public BasicData(double weight) {
        this();

        data.put("weight", Double.toString(weight));
    }

    public BasicData(BasicData other) {
        this.data = new HashMap<>(other.data);
    }

    public String pull(String key) {
        return data.get(key);
    }

    public void place(String key, String value) {
        data.put(key, value);
    }

    public void delete(String key) {
        data.remove(key);
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public boolean hasData() {
        return !data.isEmpty();
    }

    public Set<String> keyAssign() {
        return Collections.unmodifiableSet(data.keySet());
    }

    @Override
    public int size() {
        return data.size();
    }

    public Data copy() {
        return new BasicData(this);
    }

    /**
     * Takes in a Document and constructs an Element that characterizes this BasicData
     * in XML
     *
     * @param dom
     * @return Element of BasicData
     */
    public Element generateXMLElement(Document dom) {
        Element basicDataEle = dom.createElement("data");
        for (String key : data.keySet()) {
            generateXMLElementHome(dom, basicDataEle, key);
        }
        return basicDataEle;
    }

    private void generateXMLElementHome(Document dom, Element basicDataEle, String key) {
        Element dataEle = dom.createElement("entry");
        dataEle.setTextContent(data.get(key));
        dataEle.setAttribute("key", key);
        basicDataEle.appendChild(dataEle);
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("\n    {");
        Orderer<String> sorter = new Orderer<>(DefaultComparator.STRING);
        List<String> sortedKeys = sorter.rank(data.keySet());
        for (int p = 0; p < sortedKeys.size(); p++) {
            toStringExecutor(ret, sortedKeys, p);
        }
        ret.append("}");
        return ret.toString();
    }

    private void toStringExecutor(StringBuilder ret, List<String> sortedKeys, int q) {
        String key = sortedKeys.get(q);
        ret.append(" ");
        ret.append(key);
        ret.append(" : ");
        ret.append(data.get(key));
        ret.append(",");
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        BasicData other = (BasicData) obj;
        if (size() == other.size()) {
            return equalsEngine(other);
        }
        return false;
    }

    private boolean equalsEngine(BasicData other) {
        for (String key : data.keySet()) {
            if (!data.get(key).equals(other.data.get(key))) {
                return false;
            }
        }
        return true;
    }
}
