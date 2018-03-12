package com.roboticcusp.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Data structure that combines priority queue with ability to remove elements,
 * specifically encompassing both the priority queue and the OPEN set in the A* algorithm
 */
public class MyPriorityQueue {
    private PriorityQueue<PriorityNode> queue;
    // we can't remove items (other than the top) from the queue, so we keep track of it's true elements in a hashmap, representing set OPEN from A*
    private Map<Integer, PriorityNode> map;

    public MyPriorityQueue() {
        queue = new PriorityQueue<>();
        map = new HashMap<>();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    /*
     * remove and return the top priority node in the queue (assuming it's really there.)
     */
    public PriorityNode poll() {
        PriorityNode first = queue.poll();
        while (!map.containsValue(first)) // make sure we get something that's actually still in the set
        {
            first = queue.poll();
        }
        map.remove(first.takeId()); // remove returned item from set
        return first;
    }


    /**
     * add t to queue if there isn't already something there with the same id, OR if t has lower rank than existing item with same id
     */
    public boolean addIfUseful(PriorityNode t) {
        int id = t.takeId();
        if (!map.containsKey(id) || (t.fetchRank() < map.get(id).fetchRank())) { // if we already have a node with the same id
            map.remove(id); // remove current incarnation if there is one
            queue.add(t);
            map.put(id, t);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return map.toString();
    }

	/*
	 * ******************************************************************************************************************************
	 *  The following aren't actually used in A* but one would expect to find them in a priority queue, a little extra unused code:
	 * *****************************************************************************************************************************
	 */

    /*
     * add node without regard to what's already in there
     */
    public void add(PriorityNode t) {
        queue.add(t);
        map.put(t.takeId(), t);
    }

    public void remove(PriorityNode t) {
        map.remove(t.takeId()); // we can't remove it from the queue, but we'll know to ignore it if it's in there
    }

    public boolean contains(PriorityNode t) {
        return map.containsKey(t.takeId());
    }

    /**
     * return, but don't remove, the top priority item from queue
     **/
    public PriorityNode peekFirst() {
        PriorityNode first = queue.peek();
        while (!map.containsValue(first)) // make sure we get something that's actually still in the map
        {
            queue.poll(); // remove top item from the queue, as it's not in the map anyway
            first = queue.peek();
        }
        return first;
    }
}
