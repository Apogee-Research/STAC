package com.cyberpointllc.stac.hashmap;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Comparator;

/**
 * This hashmap implementation uses a linked list for each bin,
 * until there are too many items in a bin, at which point it
 * changes to a tree.
 *
 * @param <K>
 * @param <V>
 */
public class HashMap<K, V> extends AbstractMap<K, V> {
    // the actual hash table!
    transient Node<K, V>[] table;

    static final transient int DEFAULT_INITIAL_CAPACITY = 16;

    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    static final int MAXIMUM_CAPACITY = 1 << 30;

    static final int MIN_TREEIFY_CAPACITY = 64;

    static final int TREEIFY_THRESHOLD = 8;

    float loadFactor = DEFAULT_LOAD_FACTOR;

    // how big the table currently is
    transient int capacity = DEFAULT_INITIAL_CAPACITY;

    int threshold = 0;

    /**
     * Holds cached entrySet(). Note that AbstractMap fields are used
     * for keySet() and values().
     */
    transient Set<Map.Entry<K, V>> entrySet = new  TreeSet(new  NodeComparator());

    //The number of key-value mappings contained in this map.
    transient int size = 0;

    public HashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Copy constructor
     *
     * @param m
     */
    public HashMap(Map<? extends K, ? extends V> m) {
        this();
        this.putAll(m);
    }

    public HashMap(int capacity) {
        this(capacity, DEFAULT_LOAD_FACTOR);
    }

    public HashMap(int capacity, float loadFactor) {
        this.capacity = capacity;
        this.loadFactor = loadFactor;
        @SuppressWarnings({ "rawtypes", "unchecked" }) Node<K, V>[] newTable = (Node<K, V>[]) new Node[capacity];
        table = newTable;
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return entrySet;
    }

    public V put(K key, V value) {
        V e = null;
        int h = hash(key);
        Node<K, V> node = table[h];
        if (node == null) {
            // have nothing for hash h
            node = new  Node(hash(key), key, value, null);
            table[h] = node;
            entrySet.add(node);
            size++;
        } else if (node instanceof TreeNode) {
            TreeNode<K, V> treeNode, result;
            treeNode = (TreeNode<K, V>) node;
            result = treeNode.putTreeVal(table, hash(key), key, value);
            if (result == null) {
                entrySet.add(new  AbstractMap.SimpleEntry<K, V>(key, value));
                size++;
                return null;
            } else if (result.value != value) {
                entrySet.remove(result);
                // save old value for return
                e = result.value;
                // update value
                result.setValue(value);
                entrySet.add(result);
            }
        } else {
            // not a treenode
            int bincount = 0;
            while (node.next != null && !node.key.equals(key)) {
                // find either the last node, or one with the same key
                // follow the linked list until we find the key or reach the end
                node = node.next;
                bincount++;
            }
            if (node.key.equals(key)) {
                // if we found the same key, update the value
                e = node.value;
                node.value = value;
            } else {
                putHelper(node, value, bincount, h, key);
            }
        }
        if (size > capacity * DEFAULT_LOAD_FACTOR && size < this.MAXIMUM_CAPACITY) {
            // if table is getting too full, make it bigger
            resize();
        }
        return e;
    }

    @Override
    public V get(Object key) {
        int h = hash((K) key);
        Node<K, V> node = table[h];
        if (node == null) {
            return null;
        }
        if (node instanceof TreeNode) {
            TreeNode<K, V> n = (TreeNode<K, V>) node;
            n = n.getTreeNode(h, key);
            if (n == null)
                return null;
            return n.getValue();
        } else {
            while (node.next != null && !node.key.equals(key)) {
                node = node.next;
            }
            if (node.key.equals(key)) {
                return node.value;
            } else {
                return null;
            }
        }
    }

    @Override
    public boolean containsKey(Object key) {
        V val = get(key);
        if (val == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public V remove(Object key) {
        int h = hash((K) key);
        Node<K, V> node = table[h];
        Node<K, V> prev = null;
        if (node == null) {
            return null;
        }
        if (node instanceof TreeNode) {
            TreeNode<K, V> treenode = (TreeNode<K, V>) node;
            TreeNode<K, V> nodeToRemove = treenode.getTreeNode(h, key);
            if (nodeToRemove == null) {
                return null;
            }
            nodeToRemove.removeTreeNode(table, true);
            size--;
            entrySet.remove(new  AbstractMap.SimpleEntry<K, V>((K) key, nodeToRemove.value));
            return treenode.value;
        }
        // not a treenode; remove from linked list
        while (node.next != null && !node.key.equals(key)) {
            // go through linkedlist until we find it or reach the end
            prev = node;
            node = node.next;
        }
        if (node.key.equals(key)) {
            if (prev == null) {
                // first node from this hash
                table[h] = node.next;
            } else {
                prev.next = node.next;
            }
            entrySet.remove(node);
            size--;
            return node.value;
        } else {
            // key not found
            return null;
        }
    }

    private int hash(K key) {
        return Node.hash(key, capacity);
    }

    final Node<K, V>[] resize() {
        Node<K, V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        HashMapHelper0 conditionObj0 = new  HashMapHelper0(0);
        HashMapHelper1 conditionObj1 = new  HashMapHelper1(0);
        if (oldCap > conditionObj0.getValue()) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            } else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY)
                // double threshold
                newThr = oldThr << 1;
        } else if (// initial capacity was placed in threshold
        oldThr > conditionObj1.getValue())
            newCap = oldThr;
        else {
            // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int) (DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        HashMapHelper2 conditionObj2 = new  HashMapHelper2(0);
        if (newThr == conditionObj2.getValue()) {
            float ft = (float) newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float) MAXIMUM_CAPACITY ? (int) ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({ "rawtypes", "unchecked" }) Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];
        capacity = newCap;
        table = newTab;
        if (oldTab != null) {
            resizeHelper();
        }
        return newTab;
    }

    private void treeify(Node<K, V>[] tab, int index) {
        treeifyHelper(index, tab);
    }

    class NodeComparator implements Comparator {

        public int compare(Object a, Object b) {
            if (a instanceof Map.Entry && b instanceof Map.Entry) {
                Map.Entry ae = (Map.Entry) a;
                Map.Entry be = (Map.Entry) b;
                Object ak = ae.getKey();
                Object av = ae.getValue();
                Object bk = be.getKey();
                Object bv = be.getValue();
                if ((ak.equals(bk)) && ((av == null && bv == null) || av.equals(bv))) {
                    return 0;
                } else {
                    int avHash = 0;
                    int bvHash = 0;
                    if (av != null) {
                        avHash = av.hashCode();
                    }
                    if (bv != null) {
                        bvHash = bv.hashCode();
                    }
                    if ((ak.hashCode() < bk.hashCode()) || ((ak.hashCode() == bk.hashCode()) && (avHash < bvHash))) {
                        return -1;
                    } else
                        return 1;
                }
            } else
                return Integer.compare(a.hashCode(), b.hashCode());
        }
    }

    final class HashMapHelper0 {

        public HashMapHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    final class HashMapHelper1 {

        public HashMapHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    final class HashMapHelper2 {

        public HashMapHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private void putHelper(Node<K, V> node, V value, int bincount, int h, K key) {
        // if it's not the same key, add new entry on the end
        node.next = new  Node(hash(key), key, value, null);
        entrySet.add(node.next);
        if (bincount > TREEIFY_THRESHOLD) {
            treeify(table, h);
        }
        size++;
    }

    private final void resizeHelper() {
        Set<Map.Entry<K, V>> oldEntries = entrySet();
        entrySet = new  TreeSet<Map.Entry<K, V>>(new  NodeComparator());
        for (Map.Entry<K, V> entry : oldEntries) {
            put(entry.getKey(), entry.getValue());
        }
    }

    private void treeifyHelper(int index, Node<K, V>[] tab) {
        int n;
        Node<K, V> e;
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();
        else if ((e = tab[index]) != null) {
            TreeNode<K, V> hd = null, tl = null;
            do {
                TreeNode<K, V> p = new  TreeNode(e.hash, e.key, e.value, null);
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            if ((tab[index] = hd) != null)
                hd.treeify(tab);
        }
    }
}
