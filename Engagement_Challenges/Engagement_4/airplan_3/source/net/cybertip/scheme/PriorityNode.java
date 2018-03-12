package net.cybertip.scheme;

/**
 * Class to store node id and its priority for A* algorithm
 */
class PriorityNode implements Comparable<PriorityNode> {
    private int id; // node id
    private double rank; // "priority" for PriorityQueue (lower is better)
    private int goal; // the target node id in the shortest path problem

    public PriorityNode(int nodeId, double rank, int goal) {
        this.id = nodeId;
        this.rank = rank;
        this.goal = goal;
    }

    public int pullId() {
        return id;
    }

    public double obtainRank() {
        return rank;
    }

    public int compareTo(PriorityNode other) {
        int res = Double.compare(this.rank, other.rank); //(int)(this.rank - other.rank); // smaller values have priority
        if (res == 0) // resolve tie in favor of goal node if either is the goal
        {
            Integer x = compareToAdviser(other);
            if (x != null) return x;
        }
        return res;
    }

    private Integer compareToAdviser(PriorityNode other) {
        if (this.id == goal) {
            return 1;
        } else if (other.pullId() == goal) {
            return -1;
        }
        return null;
    }

    @Override
    public String toString() {
        return "(id: " + id + ", rank " + rank + ")";
    }
}
