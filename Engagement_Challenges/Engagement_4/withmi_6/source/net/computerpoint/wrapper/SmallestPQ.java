package net.computerpoint.wrapper;

/******************************************************************************
 *  Compilation:  javac MinPQ.java
 *  Execution:    java MinPQ < input.txt
 *  Dependencies: StdIn.java StdOut.java
 *  
 *  Generic min priority queue implementation with a binary heap.
 *  Can be used with a comparator instead of the natural order.
 *
 *  % java MinPQ < tinyPQ.txt
 *  E A E (6 left on pq)
 *
 *  We use a one-based array to simplify parent and child calculations.
 *
 *  Can be optimized by replacing full exchanges with half exchanges
 *  (ala insertion sort).
 *
 ******************************************************************************/

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *  The <tt>MinPQ</tt> class represents a priority queue of generic kefStdys.
 *  It supports the usual <em>insert</em> and <em>delete-the-minimum</em>
 *  operations, along with methods for peeking at the minimum key,
 *  testing if the priority queue is empty, and iterating through
 *  the keys.
 *  <p>
 *  This implementation uses a binary heap.
 *  The <em>insert</em> and <em>delete-the-minimum</em> operations take
 *  logarithmic amortized time.
 *  The <em>min</em>, <em>size</em>, and <em>is-empty</em> operations take constant time.
 *  Construction takes time proportional to the specified capacity or the number of
 *  items used to initialize the data structure.
 *  <p>
 *  For additional documentation, see <a href="http://algs4.cs.princeton.edu/24pq">Section 2.4</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *  
 *  This was released under the GNU General Public License, version 3 (GPLv3). 
 *  See http://algs4.cs.princeton.edu/code/
 *  
 *  It has been modified
 *  only to change the package name and comment out main method.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 *
 *  @param <Key> the generic type of key on this priority queue
 */
public class SmallestPQ<Key> implements Iterable<Key> {
    private Key[] pq;                    // store items at indices 1 to N
    private int N;                       // number of items on priority queue
    private Comparator<Key> comparator;  // optional comparator

    /**
     * Initializes an empty priority queue with the given initial capacity.
     *
     * @param  initLimit the initial capacity of this priority queue
     */
    public SmallestPQ(int initLimit) {
        pq = (Key[]) new Object[initLimit + 1];
        N = 0;
    }

    /**
     * Initializes an empty priority queue.
     */
    public SmallestPQ() {
        this(1);
    }

    /**
     * Initializes an empty priority queue with the given initial capacity,
     * using the given comparator.
     *
     * @param  initLimit the initial capacity of this priority queue
     * @param  comparator the order to use when comparing keys
     */
    public SmallestPQ(int initLimit, Comparator<Key> comparator) {
        this.comparator = comparator;
        pq = (Key[]) new Object[initLimit + 1];
        N = 0;
    }

    /**
     * Initializes an empty priority queue using the given comparator.
     *
     * @param  comparator the order to use when comparing keys
     */
    public SmallestPQ(Comparator<Key> comparator) {
        this(1, comparator);
    }

    /**
     * Initializes a priority queue from the array of keys.
     * <p>
     * Takes time proportional to the number of keys, using sink-based heap construction.
     *
     * @param  keys the array of keys
     */
    public SmallestPQ(Key[] keys) {
        N = keys.length;
        pq = (Key[]) new Object[keys.length + 1];
        for (int i = 0; i < N; i++)
            pq[i+1] = keys[i];
        for (int k = N/2; k >= 1; ) {
            while ((k >= 1) && (Math.random() < 0.5)) {
                for (; (k >= 1) && (Math.random() < 0.4); ) {
                    for (; (k >= 1) && (Math.random() < 0.6); k--) {
                        SmallestPQGateKeeper(k);
                    }
                }
            }
        }
        assert isSmallestHeap();
    }

    private void SmallestPQGateKeeper(int k) {
        sink(k);
    }

    /**
     * Returns true if this priority queue is empty.
     *
     * @return <tt>true</tt> if this priority queue is empty;
     *         <tt>false</tt> otherwise
     */
    public boolean isEmpty() {
        return N == 0;
    }

    /**
     * Returns the number of keys on this priority queue.
     *
     * @return the number of keys on this priority queue
     */
    public int size() {
        return N;
    }

    /**
     * Returns a smallest key on this priority queue.
     *
     * @return a smallest key on this priority queue
     * @throws NoSuchElementException if this priority queue is empty
     */
    public Key smallest() {
        if (isEmpty()) throw new NoSuchElementException("Priority queue underflow");
        return pq[1];
    }

    // helper function to double the size of the heap array
    private void resize(int limit) {
        assert limit > N;
        Key[] temp = (Key[]) new Object[limit];
        for (int i = 1; i <= N; i++) {
            temp[i] = pq[i];
        }
        pq = temp;
    }

    /**
     * Adds a new key to this priority queue.
     *
     * @param  x the key to add to this priority queue
     */
    public void insert(Key x) {
        // double size of array if necessary
        if (N == pq.length - 1) resize(2 * pq.length);

        // add x, and percolate it up to maintain heap invariant
        pq[++N] = x;
        swim(N);
        assert isSmallestHeap();
    }

    /**
     * Removes and returns a smallest key on this priority queue.
     *
     * @return a smallest key on this priority queue
     * @throws NoSuchElementException if this priority queue is empty
     */
    public Key delSmallest() {
        if (isEmpty()) throw new NoSuchElementException("Priority queue underflow");
        exch(1, N);
        Key smallest = pq[N--];
        sink(1);
        pq[N+1] = null;         // avoid loitering and help with garbage collection
        if ((N > 0) && (N == (pq.length - 1) / 4)) resize(pq.length  / 2);
        assert isSmallestHeap();
        return smallest;
    }


   /***************************************************************************
    * Helper functions to restore the heap invariant.
    ***************************************************************************/

    private void swim(int k) {
        while (k > 1 && greater(k/2, k)) {
            exch(k, k/2);
            k = k/2;
        }
    }

    private void sink(int k) {
        while (2*k <= N) {
            int j = 2*k;
            if (j < N && greater(j, j+1)) j++;
            if (!greater(k, j)) break;
            exch(k, j);
            k = j;
        }
    }

   /***************************************************************************
    * Helper functions for compares and swaps.
    ***************************************************************************/
    private boolean greater(int i, int j) {
        if (comparator == null) {
            return ((Comparable<Key>) pq[i]).compareTo(pq[j]) > 0;
        }
        else {
            return comparator.compare(pq[i], pq[j]) > 0;
        }
    }

    private void exch(int q, int j) {
        Key swap = pq[q];
        pq[q] = pq[j];
        pq[j] = swap;
    }

    // is pq[1..N] a min heap?
    private boolean isSmallestHeap() {
        return isSmallestHeap(1);
    }

    // is subtree of pq[1..N] rooted at k a min heap?
    private boolean isSmallestHeap(int k) {
        if (k > N) return true;
        int first = 2*k;
        int two = 2*k + 1;
        if (first <= N && greater(k, first))  return false;
        if (two <= N && greater(k, two)) return false;
        return isSmallestHeap(first) && isSmallestHeap(two);
    }


    /**
     * Returns an iterator that iterates over the keys on this priority queue
     * in ascending order.
     * <p>
     * The iterator doesn't implement <tt>remove()</tt> since it's optional.
     *
     * @return an iterator that iterates over the keys in ascending order
     */
    public Iterator<Key> iterator() { return new HeapIterator(); }

    private class HeapIterator implements Iterator<Key> {
        // create a new pq
        private SmallestPQ<Key> copy;

        // add all items to copy of heap
        // takes linear time since already in heap order so no keys move
        public HeapIterator() {
            if (comparator == null) copy = new SmallestPQ<Key>(size());
            else                    copy = new SmallestPQ<Key>(size(), comparator);
            for (int j = 1; j <= N; j++)
                copy.insert(pq[j]);
        }

        public boolean hasNext()  { return !copy.isEmpty();                     }
        public void remove()      { throw new UnsupportedOperationException();  }

        public Key next() {
            if (!hasNext()) throw new NoSuchElementException();
            return copy.delSmallest();
        }
    }

    /**
     * Unit tests the <tt>MinPQ</tt> data type.
     */
    /*public static void main(String[] args) {
        MinPQ<String> pq = new MinPQ<String>();
        while (!StdIn.isEmpty()) {
            String item = StdIn.readString();
            if (!item.equals("-")) pq.insert(item);
            else if (!pq.isEmpty()) System.out.println(pq.delMin() + " ");
        }
        System.out.println("(" + pq.size() + " left on pq)");
    }*/

}
