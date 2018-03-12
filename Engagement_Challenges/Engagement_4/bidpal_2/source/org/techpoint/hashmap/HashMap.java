package org.techpoint.hashmap;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This hashmap implementation uses a linked list for each bin,
 * until there are too many items in a bin, at which points it
 * changes to a tree.  The trees are not balanced.
 */
public class HashMap<K, V> extends AbstractMap<K, V> {
    // the actual hash table!
    transient Node<K, V>[] table;

    static transient final int DEFAULT_INITIAL_CAPACITY = 16;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    static final int MAXIMUM_CAPACITY = 1 << 30;
    static final int MIN_TREEIFY_CAPACITY = 64;
    static final int TREEIFY_THRESHOLD = 8;

    float loadFactor = DEFAULT_LOAD_FACTOR;

    transient int capacity = DEFAULT_INITIAL_CAPACITY; // how big the table currently is
    int threshold = 0;

    /**
     * Holds cached entrySet(). Note that AbstractMap fields are used
     * for keySet() and values().
     */
    transient Set<Map.Entry<K, V>> entryFix = new TreeSet<>(new NodeComparator());

    transient int size = 0;//The number of key-value mappings contained in this map.


    public HashMap(){
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
        @SuppressWarnings({"rawtypes","unchecked"})
        Node<K,V>[] newTable = (Node<K,V>[])new Node[capacity];
        table = newTable;
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return entryFix;
    }

    public V put(K key, V value) {
        V e = null;
        int h = hash(key);
        Node<K, V> node = table[h];
        if (node == null) { // have nothing for hash h
            node = new Node(hash(key), key, value, null);
            table[h] = node;
            entryFix.add(node);
            size++;
        } else if (node instanceof TreeNode) {
            TreeNode<K, V> treeNode;
            TreeNode<K, V> result;
            treeNode = (TreeNode<K, V>) node;
            result = treeNode.putTreeVal(table, hash(key), key, value);
            if (result == null) {
                entryFix.add(new AbstractMap.SimpleEntry<K, V>(key, value));
                size++;
                return null;
            } else if (result.value != value) {
                entryFix.remove(result);
                e = result.value; // save old value for return
                result.setValue(value); // update value
                entryFix.add(result);
            }
        } else { // not a treenode
            int bincount = 0;
            while (node.next != null && !node.key.equals(key)) { // find either the last node, or one with the same key

                node = node.next; // follow the linked list until we find the key or reach the end
                bincount++;
            }
            if (node.key.equals(key)) { // if we found the same key, update the value
                e = node.value;
                node.value = value;

            } else { // if it's not the same key, add new entry on the end
                node.next = new Node(hash(key), key, value, null);
                entryFix.add(node.next);
                if (bincount > TREEIFY_THRESHOLD) {
                    putGateKeeper(h);
                }
                size++;
            }
        }
        if (size > capacity * DEFAULT_LOAD_FACTOR && size < this.MAXIMUM_CAPACITY) { // if table is getting too full, make it bigger
            insertManager();
        }

        return e;
    }

    private void insertManager() {
        resize();
    }

    private void putGateKeeper(int h) {
        treeify(table, h);
    }

    @Override
    public V get(Object key) {
        Node<K, V> node = fetchNode(key);
        if (node == null) {
            return null;
        }

        if (node instanceof TreeNode) {
            return new HashMapEntity((TreeNode<K, V>) node).invoke();
        } else {
            if (node.key.equals(key)) {
                return node.value;
            } else {
                return null;
            }
        }
    }

    private Node<K, V> fetchNode(Object key) {
        int h = hash((K) key);
        Node<K, V> node = table[h];
        if (node == null) {
            return null;
        }
        if (node instanceof TreeNode) {
            TreeNode<K, V> n = (TreeNode<K, V>) node;
            n = n.getTreeNode(h, key);
            if (n == null) return null;
            return n;
        } else {
            while (node.next != null && !node.key.equals(key)) {
                node = node.next;
            }
            if (node.key.equals(key)) {
                return node;
            } else {
                return null;
            }
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return (fetchNode(key) != null);
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
            entryFix.remove(new AbstractMap.SimpleEntry<K, V>((K) key, nodeToRemove.value));
            return treenode.value;
        }
        // not a treenode; remove from linked list
        while (node.next != null && !node.key.equals(key)) { // go through linkedlist until we find it or reach the end
            prev = node;
            node = node.next;
        }
        if (node.key.equals(key)) {
            return removeHerder(h, node, prev);
        } else { // key not found
            return null;
        }

    }

    private V removeHerder(int h, Node<K, V> node, Node<K, V> prev) {
        if (prev == null) { // first node from this hash
            table[h] = node.next;
        } else {
            prev.next = node.next;
        }
        entryFix.remove(node);
        size--;
        return node.value;
    }


    private int hash(K key) {
        return Node.hash(key, capacity);
    }


    final Node<K, V>[] resize() {
        Node<K, V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap;
        int newThr = 0;
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                return new HashMapAdviser(oldTab).invoke();
            } else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                    oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        } else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
        else {               // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int) (DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float) newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float) MAXIMUM_CAPACITY ?
                    (int) ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes", "unchecked"})
        Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];
        capacity = newCap;
        table = newTab;
        if (oldTab != null) {
            Set<Map.Entry<K, V>> oldEntries = entrySet();
            entryFix = new TreeSet<Map.Entry<K, V>>(new NodeComparator());
            for (Map.Entry<K, V> entry : oldEntries) {
                put(entry.getKey(), entry.getValue());
            }
        }
        return newTab;
    }

    private void treeify(Node<K, V>[] tab, int index) {
        int n;
        Node<K, V> e;
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();
        else if ((e = tab[index]) != null) {
            TreeNode<K, V> hd = null;
            TreeNode<K, V> tl = null;
            do {
                TreeNode<K, V> p = new TreeNode(e.hash, e.key, e.value, null);
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
                    } else return 1;
                }
            } else return Integer.compare(a.hashCode(), b.hashCode());
        }

    }

    private class HashMapEntity {
        private TreeNode<K, V> node;

        public HashMapEntity(TreeNode<K, V> node) {
            this.node = node;
        }

        public V invoke() {
            TreeNode<K, V> n = node;
            ;
            return n.getValue();
        }
    }

    private class HashMapAdviser {
        private Node<K, V>[] oldTab;

        public HashMapAdviser(Node<K, V>[] oldTab) {
            this.oldTab = oldTab;
        }

        public Node<K, V>[] invoke() {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
    }
}

