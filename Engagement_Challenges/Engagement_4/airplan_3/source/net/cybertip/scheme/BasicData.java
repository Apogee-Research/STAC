package net.cybertip.scheme;

import net.cybertip.align.DefaultComparator;
import net.cybertip.align.Sorter;
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

    public Set<String> keySet() {
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
    public Element makeXMLElement(Document dom) {
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
        Sorter<String> sorter = new Sorter<>(DefaultComparator.STRING);
        List<String> sortedKeys = sorter.arrange(data.keySet());
        for (int a = 0; a < sortedKeys.size(); a++) {
            String key = sortedKeys.get(a);
            ret.append(" ");
            ret.append(key);
            ret.append(" : ");
            ret.append(data.get(key));
            ret.append(",");
        }
        ret.append("}");
        return ret.toString();
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
            for (String key : data.keySet()) {
                if (equalsGateKeeper(other, key)) return false;
            }
            return true;
        }
        return false;
    }

    private boolean equalsGateKeeper(BasicData other, String key) {
        if (!data.get(key).equals(other.data.get(key))) {
            return true;
        }
        return false;
    }
}
