package com.tweeter.utility;

/**
 * This represents an edge in the Trie data-structure.
 */
class TrieEdge {
    public TrieEdge(String label) {
        this.label = label;
    }
    public TrieEdge targetEdge(int i) {
        return this.target.edges.get(i);
    }
    public int targetEdgesSize() {
        return this.target.edges.size();
    }
    public String targetEdgeLabel(int i) {
        return this.target.edges.get(i).label;
    }
    public boolean targetLeaf() {
        return this.target.leaf();
    }
    TrieNode target = new TrieNode();
    String label = null;
}
