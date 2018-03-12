package com.roboticcusp.mapping;

import com.roboticcusp.rank.ArrangerBuilder;
import com.roboticcusp.rank.DefaultComparator;
import com.roboticcusp.rank.Arranger;
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

    public String grab(String key) {
        return data.get(key);
    }

    public void put(String key, String value) {
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

    public Set<String> keyDefine() {
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
    public Element composeXMLElement(Document dom) {
        Element basicDataEle = dom.createElement("data");
        for (String key : data.keySet()) {
            Element dataEle = dom.createElement("entry");
            dataEle.setTextContent(data.get(key));
            dataEle.setAttribute("key", key);
            basicDataEle.appendChild(dataEle);
        }
        return basicDataEle;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("\n    {");
        Arranger<String> sorter = new ArrangerBuilder().defineComparator(DefaultComparator.STRING).composeArranger();
        List<String> sortedKeys = sorter.arrange(data.keySet());
        for (int q = 0; q < sortedKeys.size(); q++) {
            toStringFunction(ret, sortedKeys, q);
        }
        ret.append("}");
        return ret.toString();
    }

    private void toStringFunction(StringBuilder ret, List<String> sortedKeys, int b) {
        String key = sortedKeys.get(b);
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
            return equalsSupervisor(other);
        }
        return false;
    }

    private boolean equalsSupervisor(BasicData other) {
        for (String key : data.keySet()) {
            if (equalsSupervisorCoach(other, key)) return false;
        }
        return true;
    }

    private boolean equalsSupervisorCoach(BasicData other, String key) {
        if (!data.get(key).equals(other.data.get(key))) {
            return true;
        }
        return false;
    }
}
