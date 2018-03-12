package com.tweeter.utility;

import java.util.ArrayList;

/**
 * Trie nodes represent characters.
 *
 * A path from the root node to a leaf node spells out a single word.
 */
public class TrieNode {
    ArrayList<TrieEdge> edges = new ArrayList<>();

    public TrieEdge getEdge(int i) {
        return this.edges.get(i);
    }

    public String edgeLabel(int i) {
        return this.edges.get(i).label;
    }

    public TrieNode getEdgeTarget(int i) {
        return this.edges.get(i).target;
    }

    public boolean edgeTargetLeaf(int i) {
        return this.edges.get(i).target.leaf();
    }

    public int countEdges() {
        return this.edges.size();
    }

    public boolean leaf() {
        return this.edges.size() == 0;
    }

    public static boolean prefix(String needle, String label, int elementsFound) {
        if (needle.length() > elementsFound) {
            char nc = needle.charAt(elementsFound);
            char lc = label.charAt(0);
            return nc == lc;
        }
        return false;
    }

    private TrieEdge findOne(String needle, int elementsFound) {
        for (TrieEdge edge : this.edges) {
            if (prefix(needle, edge.label, elementsFound)) {
                return edge;
            }
        }
        return null;
    }

    public boolean lookup(String needle) {
        TrieNode traverseNode = this;
        int elementsFound = 0;

        while (traverseNode != null && !traverseNode.leaf() && elementsFound < needle.length()) {
            TrieEdge nextEdge = traverseNode.findOne(needle, elementsFound);
            if (nextEdge != null) {
                if (needle.length() - elementsFound > nextEdge.label.length()) {
                    traverseNode = nextEdge.target;
                    elementsFound += nextEdge.label.length();
                } else {
                    int inc = 0;
                    for (int i = 0; i < needle.length() - elementsFound; i++) {
                        if (needle.charAt(elementsFound + i) == nextEdge.label.charAt(i)) inc++;
                    }
                    elementsFound += inc;
                    if (elementsFound < needle.length()) {
                        break; // This should break the loop so we don't try to re-search the same edge.
                    }
                }
            } else {
                traverseNode = null;
            }
        }

        if (traverseNode != null) {
            if (traverseNode.leaf() && elementsFound == needle.length()) {
                return true;
            } else if (elementsFound >= needle.length()) {
                return true;
            }
        }

        return false;
    }

    private static String shares(String a, String b) {
        for (int i = 0; i < (a.length() >= b.length() ? b.length() : a.length()); i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return a.substring(0, i);
            }
        }
        return (a.length() >= b.length() ? b : a);
    }

    public void insert(String needle) {
        TrieNode traverseNode = this, lastNode = null;
        int lastElementsFound = 0;
        int elementsFound = 0;
        int treeLength = 0;

        TrieEdge nextEdge = null;

        while (elementsFound == treeLength &&
                (nextEdge = traverseNode.findOne(needle, elementsFound)) != null &&
                !traverseNode.leaf()) {
            lastNode = traverseNode;
            traverseNode = nextEdge.target;
            lastElementsFound = elementsFound;
            elementsFound += shares(needle.substring(elementsFound), nextEdge.label).length();
            treeLength += nextEdge.label.length();
        }

        if (elementsFound < needle.length()) {
            if (elementsFound < treeLength) {
                // Cut and rebuild.
                String needleSub = needle.substring(elementsFound);
                TrieEdge newEdge = new TrieEdge(needleSub);
                TrieEdge earlyEdge = new TrieEdge(needle.substring(lastElementsFound, elementsFound));

                earlyEdge.target.edges.add(newEdge);
                int i = 0;
                for (; i < lastNode.edges.size(); i++) {
                    TrieEdge thisEdge = lastNode.edges.get(i);
                    if (thisEdge.label.startsWith(earlyEdge.label)) {
                        String teSub = thisEdge.label.substring(elementsFound - lastElementsFound);
                        if (teSub.length() > 0) {
                            TrieEdge latest = new TrieEdge(teSub);
                            earlyEdge.target.edges.add(latest);
                            lastNode.edges.remove(thisEdge);
                            for (TrieEdge edge : thisEdge.target.edges) {
                                latest.target.edges.add(edge);
                            }
                        } else {
                            lastNode.edges.remove(thisEdge);
                        }
                        break;
                    }
                }

                lastNode.edges.add(earlyEdge);
            } else if (elementsFound == treeLength) {
                TrieEdge trieEdge = new TrieEdge(needle.substring(elementsFound));
                traverseNode.edges.add(trieEdge);
            }
        } else if (elementsFound == needle.length() && elementsFound < treeLength) {
            TrieEdge firstEdge = new TrieEdge(needle.substring(lastElementsFound, elementsFound));
            TrieEdge edgeReplace = new TrieEdge(nextEdge.label.substring(firstEdge.label.length()));

            lastNode.edges.add(firstEdge);

            edgeReplace.target = nextEdge.target;

            firstEdge.target.edges.add(edgeReplace);

            String needleSub = needle.substring(firstEdge.label.length());
            if (needleSub.length() > 0) {
                TrieEdge newEdge = new TrieEdge(needleSub);
                firstEdge.target.edges.add(newEdge);
            }

            lastNode.edges.remove(nextEdge);
        }
    }
}
