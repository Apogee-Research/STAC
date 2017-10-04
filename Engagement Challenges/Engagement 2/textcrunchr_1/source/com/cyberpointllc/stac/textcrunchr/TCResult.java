package com.cyberpointllc.stac.textcrunchr;

import java.util.ArrayList;

public class TCResult {

    private String name;

    private String value;

    // user can either give us a string value all at once, or incrementally add
    // values to a List
    private ArrayList<Component> results;

    // Little helper class so we can use a list instead of LinkedHashMap (which is vulnerable) for storing result components
    class Component {

        String key;

        String val;

        Component(String k, String v) {
            key = k;
            val = v;
        }
    }

    TCResult(String name, String value) {
        this.name = name;
        this.value = value;
    }

    TCResult(String name) {
        this.name = name;
        // value will be set by calling addResult
        this.value = null;
        this.results = new  ArrayList<Component>();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        if (value == null) {
            String lineSeparator = System.lineSeparator();
            StringBuilder builder = new  StringBuilder();
            for (Component c : results) {
                builder.append(c.key).append(": ").append(c.val).append(lineSeparator);
            }
            value = builder.toString();
        }
        return value;
    }

    public void addResult(String key, int val) {
        addResultHelper(val, key);
    }

    public void addResult(String key, double val) {
        results.add(new  Component(key, Double.toString(val)));
    }

    public void addResult(String key, String val) {
        results.add(new  Component(key, val));
    }

    private void addResultHelper(int val, String key) {
        results.add(new  Component(key, Integer.toString(val)));
    }
}
