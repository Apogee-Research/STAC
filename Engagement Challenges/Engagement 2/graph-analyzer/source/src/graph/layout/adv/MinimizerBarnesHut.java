package graph.layout.adv;

//Copyright (C) 2008 Andreas Noack
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA 
import graph.Edge;
import graph.Node;
import java.util.*;

/**
 * Minimizer for the LinLog energy model and its generalizations, for computing
 * two- and three-dimensional graph layouts. Based on the Barnes-Hut algorithm.
 * For more information about the LinLog energy model, see the (freely
 * downloadable) article Andreas Noack:
 * <a href="http://jgaa.info/volume11.html">
 * "Energy Models for Graph Clustering"</a>, Journal of Graph Algorithms and
 * Applications 11(2):453-480, 2007.
 *
 * @author Andreas Noack (an@informatik.tu-cottbus.de)
 * @version 14.11.2008
 */
public class MinimizerBarnesHut {

    /**
     * Nodes with weights specifying their repulsion strength.
     */
    private final List<Node> nodes;
    /**
     * Factor for repulsion energy.
     */
    private double repuFactor;
    /**
     * Exponent of the Euclidean distance in the repulsion energy.
     */
    private double repuExponent;

    /**
     * Attraction edges for each node.
     */
    private final Map<Node, Collection<Edge>> attrEdges;
    /**
     * Exponent of the Euclidean distance in the attraction energy.
     */
    private double attrExponent;

    /**
     * Position of the barycenter of the nodes.
     */
    private final double[] baryCenter = new double[3];
    /**
     * Factor for the gravitation energy = attraction to the barycenter. Set to
     * 0.0 for no gravitation.
     */
    private double gravFactor;

    /**
     * Number of coordinates of each node.
     */
    private final int nrDims = 3;
    /**
     * Position in 3-dimensional space for each node.
     */
    private Map<Node, double[]> positions;

    /**
     * Initializes the attributes.
     *
     * @param nodes nodes with weights specifying their repulsion strengths.
     * More precisely, the repulsion between every pair of different nodes is
     * proportional to the product of their weights. It is recommended to use
     * so-called "edge-repulsion", which means to set the weight of each node to
     * the sum of the weights of its attraction edges. Weights must not be
     * negative.
     * @param attrEdges attraction edges. Omit edges with weight 0.0 (i.e.
     * non-edges). For unweighted graphs use weight 1.0 for all edges. Weights
     * must not be negative. Weights must be symmetric, i.e. the weight from
     * node <code>n1</code> to node <code>n2</code> must be equal to the weight
     * from node <code>n2</code> to node <code>n1</code>.
     * @param repuExponent exponent of the distance in the repulsion energy.
     * Exception: The value 0.0 corresponds to logarithmic repulsion. Is 0.0 in
     * both the LinLog and the Fruchterman-Reingold energy model. Negative
     * values are permitted.
     * @param attrExponent exponent of the distance in the attraction energy. Is
     * 1.0 in the LinLog model (which is used for computing clusters, i.e. dense
     * subgraphs), and 3.0 in standard energy model of Fruchterman and Reingold.
     * Must be greater than <code>repuExponent</code>.
     * @param gravFactor factor for the gravitation energy. Gravitation attracts
     * each node to the barycenter of all nodes, to prevent distances between
     * unconnected graph components from approaching infinity. Typical values
     * are 0.0 if the graph is guaranteed to be connected, and positive values
     * significantly smaller 1.0 (e.g. 0.05) otherwise.
     */
    public MinimizerBarnesHut(
            final Collection<Node> nodes, final Collection<Edge> attrEdges,
            final double repuExponent, final double attrExponent, final double gravFactor) {
        this.nodes = new ArrayList<Node>(nodes);
        this.attrEdges = new HashMap<Node, Collection<Edge>>();
        for (Node node : nodes) {
            this.attrEdges.put(node, new ArrayList<Edge>());
        }
        for (Edge edge : attrEdges) {
            if (edge.head == edge.tail) {
                continue;
            }
            this.attrEdges.get(edge.head).add(edge);
        }
        this.repuExponent = repuExponent;
        this.attrExponent = attrExponent;
        this.gravFactor = gravFactor;
    }

    /**
     * Iteratively minimizes energy using the Barnes-Hut algorithm. Starts from
     * the positions in the parameter <code>positions</code>, and stores the
     * computed positions in <code>positions</code>.
     *
     * @param positions position in 3D space for each node. Is not copied and
     * serves as input and output parameter. Each position must be a
     * <code>double[3]</code>. If the input is two-dimensional (i.e. the third
     * array element is 0.0 for all nodes), the output is also two-dimensional.
     * Different nodes with nonzero weights must have different positions.
     * Random initial positions are appropriate.
     * @param nrIterations number of iterations. Choose appropriate values by
     * observing the convergence of energy. A typical value is 100.
     */
    public void minimizeEnergy(final Map<Node, double[]> positions, final int nrIterations) {
        if (nodes.size() <= 1) {
            return;
        }
        this.positions = positions;

        initEnergyFactors();
        final double finalAttrExponent = attrExponent;
        final double finalRepuExponent = repuExponent;

        // compute initial energy
        computeBaryCenter();
        OctTree octTree = buildOctTree(); // for efficient repulsion computation
        printStatistics(octTree);
        double energySum = 0.0;
        for (Node node : nodes) {
            energySum += getEnergy(node, octTree);
        }
        org.graph.commons.logging.LogFactory.getLog(null).info("initial energy " + energySum);

        // minimize energy
        final double[] oldPos = new double[nrDims];
        final double[] bestDir = new double[nrDims];
        for (int step = 1; step <= nrIterations; step++) {
            computeBaryCenter();
            octTree = buildOctTree();

            if (nrIterations >= 50 && finalRepuExponent < 1.0) {
                attrExponent = finalAttrExponent;
                repuExponent = finalRepuExponent;
                if (step <= 0.6 * nrIterations) {
                    // use energy model with few local minima 
                    attrExponent += 1.1 * (1.0 - finalRepuExponent);
                    repuExponent += 0.9 * (1.0 - finalRepuExponent);
                } else if (step <= 0.9 * nrIterations) {
                    // gradually move to final energy model
                    attrExponent += 1.1 * (1.0 - finalRepuExponent)
                            * (0.9 - ((double) step) / nrIterations) / 0.3;
                    repuExponent += 0.9 * (1.0 - finalRepuExponent)
                            * (0.9 - ((double) step) / nrIterations) / 0.3;
                }
            }

            // move each node
            energySum = 0.0;
            for (Node node : nodes) {
                final double oldEnergy = getEnergy(node, octTree);

                // compute direction of the move of the node
                getDirection(node, octTree, bestDir);

                // line search: compute length of the move
                double[] pos = positions.get(node);
                for (int d = 0; d < nrDims; d++) {
                    oldPos[d] = pos[d];
                }
                double bestEnergy = oldEnergy;
                int bestMultiple = 0;
                for (int d = 0; d < nrDims; d++) {
                    bestDir[d] /= 32;
                }
                for (int multiple = 32;
                        multiple >= 1 && (bestMultiple == 0 || bestMultiple / 2 == multiple);
                        multiple /= 2) {
                    octTree.removeNode(node, pos, 0);
                    for (int d = 0; d < nrDims; d++) {
                        pos[d] = oldPos[d] + bestDir[d] * multiple;
                    }
                    octTree.addNode(node, pos, 0);
                    double curEnergy = getEnergy(node, octTree);
                    if (curEnergy < bestEnergy) {
                        bestEnergy = curEnergy;
                        bestMultiple = multiple;
                    }
                }
                for (int multiple = 64;
                        multiple <= 128 && bestMultiple == multiple / 2;
                        multiple *= 2) {
                    octTree.removeNode(node, pos, 0);
                    for (int d = 0; d < nrDims; d++) {
                        pos[d] = oldPos[d] + bestDir[d] * multiple;
                    }
                    octTree.addNode(node, pos, 0);
                    double curEnergy = getEnergy(node, octTree);
                    if (curEnergy < bestEnergy) {
                        bestEnergy = curEnergy;
                        bestMultiple = multiple;
                    }
                }

                octTree.removeNode(node, pos, 0);
                for (int d = 0; d < nrDims; d++) {
                    pos[d] = oldPos[d] + bestDir[d] * bestMultiple;
                }
                octTree.addNode(node, pos, 0);
                energySum += bestEnergy;
            }
            org.graph.commons.logging.LogFactory.getLog(null).info("iteration " + step
                    + "   energy " + energySum
                    + "   repulsion " + repuExponent);
        }
        printStatistics(octTree);
        if (octTree.getHeight() >= OctTree.MAX_DEPTH) {
            System.err.println(
                    "The node distances in the layout are extremely nonuniform.\n"
                    + " The graph likely has unconnected or very sparsely connected components.\n"
                    + " Set random layout to recover, and increase gravitation factor.");
        }
    }

    /**
     * Computes values for the factors of the repulsion and gravitation energy,
     * <code>repuFactor</code> and <code>gravFactor</code>. Chooses
     * <code>repuFactor</code> such that the maximum distances in the resulting
     * layout approximate (very) roughly the square root of the sum of the
     * repuWeights, which is appropriate when each graph node is visualized as a
     * geometric object whose area is the node's repuWeight.
     */
    private void initEnergyFactors() {
        double attrSum = 0.0;
        for (Node node : nodes) {
            for (Edge edge : attrEdges.get(node)) {
                attrSum += edge.weight;
            }
        }
        double repuSum = 0.0;
        for (Node node : nodes) {
            repuSum += node.weight;
        }

        if (repuSum > 0.0 && attrSum > 0.0) {
            final double density = attrSum / repuSum / repuSum;
            repuFactor = density * Math.pow(repuSum, 0.5 * (attrExponent - repuExponent));
            gravFactor = density * repuSum * Math.pow(gravFactor, attrExponent - repuExponent);
        } else {
            repuFactor = 1.0;
        }
    }

    /**
     * Returns the Euclidean distance between the positions pos1 and pos2.
     *
     * @return Euclidean distance between the positions pos1 and pos2
     */
    private final double getDist(final double[] pos1, final double[] pos2) {
        double dist = 0.0;
        for (int d = 0; d < nrDims; d++) {
            double diff = pos1[d] - pos2[d];
            dist += diff * diff;
        }
        return Math.sqrt(dist);
    }

    /**
     * Returns the repulsion energy between a node and the nodes in an octtree.
     *
     * @param node repulsing node
     * @param tree octtree containing repulsing nodes
     * @return repulsion energy between the specified node and the nodes in the
     * specified octtree
     */
    private double getRepulsionEnergy(final Node node, final OctTree tree) {
        if (tree == null || tree.node == node) {
            return 0.0;
        }
        if (node.weight == 0.0) {
            return 0.0;
        }

        final double dist = getDist(positions.get(node), tree.position);
        if (tree.childCount > 0 && dist < 2.0 * tree.width()) {
            double energy = 0.0;
            for (int i = 0; i < tree.children.length; i++) {
                energy += getRepulsionEnergy(node, tree.children[i]);
            }
            return energy;
        }

        if (dist == 0.0) {
            return 0.0;
        }

        if (repuExponent == 0.0) {
            return -repuFactor * node.weight * tree.weight * Math.log(dist);
        } else {
            return -repuFactor * node.weight * tree.weight
                    * Math.pow(dist, repuExponent) / repuExponent;
        }
    }

    /**
     * Returns the attraction energy of a node.
     *
     * @param node attracting node
     * @return attraction energy of the specified node
     */
    private double getAttractionEnergy(final Node node) {
        double energy = 0.0;
        final double[] position = positions.get(node);
        for (Edge edge : attrEdges.get(node)) {
            final double dist = getDist(position, positions.get(edge.tail));
            if (attrExponent == 0.0) {
                energy += edge.weight * Math.log(dist);
            } else {
                energy += edge.weight * Math.pow(dist, attrExponent) / attrExponent;
            }
        }
        return energy;
    }

    /**
     * Returns the gravitation energy of a node.
     *
     * @param node gravitating node
     * @return gravitation energy of the specified node
     */
    private double getGravitationEnergy(final Node node) {
        final double dist = getDist(positions.get(node), baryCenter);
        if (attrExponent == 0.0) {
            return gravFactor * node.weight * Math.log(dist);
        } else {
            return gravFactor * node.weight * Math.pow(dist, attrExponent) / attrExponent;
        }
    }

    /**
     * Returns the total energy of a node.
     *
     * @param node node
     * @return total energy of the specified node
     */
    private double getEnergy(final Node node, final OctTree octTree) {
        return getRepulsionEnergy(node, octTree)
                + getAttractionEnergy(node) + getGravitationEnergy(node);
    }

    /**
     * Computes the direction of the repulsion force of an octtree on a node.
     *
     * @param node repulsing node
     * @param tree repulsing octtree
     * @param dir direction of the repulsion force acting on the node is added
     * to this variable (output parameter)
     * @return approximate second derivation of the repulsion energy
     */
    private double addRepulsionDir(final Node node, final OctTree tree, final double[] dir) {
        if (tree == null || tree.node == node) {
            return 0.0;
        }
        if (node.weight == 0.0) {
            return 0.0;
        }

        final double[] position = positions.get(node);
        final double dist = getDist(position, tree.position);
        if (tree.childCount > 0 && dist < 2.0 * tree.width()) {
            double dir2 = 0.0;
            for (int i = 0; i < tree.children.length; i++) {
                dir2 += addRepulsionDir(node, tree.children[i], dir);
            }
            return dir2;
        }

        if (dist == 0.0) {
            return 0.0;
        }

        double tmp = repuFactor * node.weight * tree.weight
                * Math.pow(dist, repuExponent - 2);
        for (int d = 0; d < nrDims; d++) {
            dir[d] -= (tree.position[d] - position[d]) * tmp;
        }
        return tmp * Math.abs(repuExponent - 1);
    }

    /**
     * Computes the direction of the attraction force on the a node.
     *
     * @param node attracting node
     * @param dir direction of the attraction force acting on the node is added
     * to this variable (output parameter)
     * @return approximate second derivation of the attraction energy
     */
    private double addAttractionDir(final Node node, final double[] dir) {
        double dir2 = 0.0;
        final double[] position = positions.get(node);
        for (Edge edge : attrEdges.get(node)) {
            final double[] position2 = positions.get(edge.tail);
            final double dist = getDist(position, position2);
            if (dist == 0.0) {
                continue;
            }
            double tmp = edge.weight * Math.pow(dist, attrExponent - 2);
            dir2 += tmp * Math.abs(attrExponent - 1);
            for (int d = 0; d < nrDims; d++) {
                dir[d] += (position2[d] - position[d]) * tmp;
            }
        }
        return dir2;
    }

    /**
     * Computes the direction of the gravitation force on the a node.
     *
     * @param node gravitating node
     * @param dir direction of the gravitation force acting on the node is added
     * to this variable (output parameter)
     * @return approximate second derivation of the gravitation energy
     */
    private double addGravitationDir(final Node node, final double[] dir) {
        final double[] position = positions.get(node);
        final double dist = getDist(position, baryCenter);
        double tmp = gravFactor * repuFactor * node.weight * Math.pow(dist, attrExponent - 2);
        for (int d = 0; d < nrDims; d++) {
            dir[d] += (baryCenter[d] - position[d]) * tmp;
        }
        return tmp * Math.abs(attrExponent - 1);
    }

    /**
     * Computes the direction of the total force acting on a node.
     *
     * @param node node
     * @param dir direction of the total force acting on the node (output
     * parameter)
     */
    private void getDirection(final Node node, final OctTree octTree, final double[] dir) {
        for (int d = 0; d < nrDims; d++) {
            dir[d] = 0.0;
        }

        double dir2 = addRepulsionDir(node, octTree, dir);
        dir2 += addAttractionDir(node, dir);
        dir2 += addGravitationDir(node, dir);

        if (dir2 != 0.0) {
            // normalize force vector with second derivation of energy
            for (int d = 0; d < nrDims; d++) {
                dir[d] /= dir2;
            }

            // ensure that the length of dir is not greater
            // than 1/16 of the octtree width,
            // to prevent the node from leaving the octtree region
            double scale = 1.0;
            for (int d = 0; d < nrDims; d++) {
                double width = octTree.maxPos[d] - octTree.minPos[d];
                if (width > 0.0) {
                    scale = Math.min(scale, Math.abs(width / 16 / dir[d]));
                }
            }
            for (int d = 0; d < nrDims; d++) {
                dir[d] *= scale;
            }
        } else {
            for (int d = 0; d < nrDims; d++) {
                dir[d] = 0.0;
            }
        }
    }

    /**
     * Builds the octtree.
     */
    private OctTree buildOctTree() {
        // compute mimima and maxima of positions in each dimension
        double[] minPos = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
        double[] maxPos = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        for (Node node : nodes) {
            if (node.weight == 0.0) {
                continue;
            }
            double[] position = positions.get(node);
            for (int d = 0; d < nrDims; d++) {
                minPos[d] = Math.min(position[d], minPos[d]);
                maxPos[d] = Math.max(position[d], maxPos[d]);
            }
        }
        // provide additional space for moving nodes
        for (int d = 0; d < nrDims; d++) {
            double posDiff = maxPos[d] - minPos[d];
            maxPos[d] += posDiff / 2;
            minPos[d] -= posDiff / 2;
        }

        // add nodes with non-zero weight to the octtree
        OctTree result = new OctTree(null, new double[nrDims], minPos, maxPos);
        for (Node node : nodes) {
            result.addNode(node, positions.get(node), 0);
        }
        return result;
    }

    /**
     * Computes the position of the barycenter of all nodes and stores it in the
     * attribute <code>baryCenter</code>.
     */
    private void computeBaryCenter() {
        for (int d = 0; d < nrDims; d++) {
            baryCenter[d] = 0.0;
        }
        double weightSum = 0.0;
        for (Node node : nodes) {
            weightSum += node.weight;
            double[] position = positions.get(node);
            for (int d = 0; d < nrDims; d++) {
                baryCenter[d] += node.weight * position[d];
            }
        }
        if (weightSum > 0.0) {
            for (int d = 0; d < nrDims; d++) {
                baryCenter[d] /= weightSum;
            }
        }
    }

    /**
     * Computes and outputs some statistics.
     */
    private void printStatistics(final OctTree octTree) {
        org.graph.commons.logging.LogFactory.getLog(null).info("Number of nodes: " + nodes.size());
        double attrSum = 0.0;
        for (Node node : nodes) {
            for (Edge edge : attrEdges.get(node)) {
                attrSum += edge.weight;
            }
        }
        org.graph.commons.logging.LogFactory.getLog(null).info("Overall attraction: " + attrSum);
        double meanAttrEnergy = 0.0;
        for (Node node : nodes) {
            meanAttrEnergy += getAttractionEnergy(node);
        }
        meanAttrEnergy = (attrExponent == 0.0)
                ? Math.exp(meanAttrEnergy / attrSum)
                : Math.pow(meanAttrEnergy * attrExponent / attrSum, 1.0 / attrExponent);
        org.graph.commons.logging.LogFactory.getLog(null).info("Weighted mean of attraction energy: " + meanAttrEnergy);

        double repuSum = 0.0, repuSquareSum = 0.0;
        for (Node node : nodes) {
            repuSum += node.weight;
            repuSquareSum += node.weight * node.weight;
        }
        repuSum = repuSum * repuSum - repuSquareSum;
        org.graph.commons.logging.LogFactory.getLog(null).info("Overall repulsion: " + repuSum);
        double meanRepuEnergy = 0.0;
        for (Node node : nodes) {
            meanRepuEnergy += getRepulsionEnergy(node, octTree);
        }
        meanRepuEnergy /= repuFactor;
        meanRepuEnergy = (repuExponent == 0.0)
                ? Math.exp(-meanRepuEnergy / repuSum)
                : Math.pow(-meanRepuEnergy * repuExponent / repuSum, 1.0 / repuExponent);
        org.graph.commons.logging.LogFactory.getLog(null).info("Weighted mean of repulsion energy: " + meanRepuEnergy);

        org.graph.commons.logging.LogFactory.getLog(null).info("Mean attraction / mean repulsion: " + meanAttrEnergy / meanRepuEnergy);
    }

    /**
     * Octtree for graph nodes with positions in 3D space. Contains all graph
     * nodes that are located in a given cuboid in 3D space.
     *
     * @author Andreas Noack
     */
    static class OctTree {

        /**
         * Maximum depth of tree nodes.
         */
        protected static final int MAX_DEPTH = 18;
        /**
         * For leafs, the corresponding graph node; for non-leafs
         * <code>null</code>.
         */
        protected Node node;
        /**
         * Children of this tree node.
         */
        protected OctTree[] children = new OctTree[8];
        /**
         * Number of children of this tree node.
         */
        protected int childCount = 0;
        /**
         * Barycenter of the contained graph nodes.
         */
        protected double[] position;
        /**
         * Total weight of the contained graph nodes.
         */
        protected double weight;
        /**
         * Minimum coordinates of the cuboid in each of the 3 dimensions.
         */
        protected double[] minPos;
        /**
         * Maximum coordinates of the cuboid in each of the 3 dimensions.
         */
        protected double[] maxPos;

        /**
         * Creates an octtree containing exactly one graph node.
         *
         * @param node graph node
         * @param position position of the graph node
         * @param minPos minimum coordinates of the cuboid
         * @param maxPos maximum coordinates of the cuboid
         */
        public OctTree(Node node, double[] position, double[] minPos, double[] maxPos) {
            this.node = node;
            this.position = new double[]{position[0], position[1], position[2]};
            this.weight = node != null ? node.weight : 0.0;
            this.minPos = minPos;
            this.maxPos = maxPos;
        }

        /**
         * Adds a graph node to the octtree.
         *
         * @param newNode graph node
         * @param newPos position of the graph node
         * @param depth depth of this tree node in the octtree
         */
        public void addNode(Node newNode, double[] newPos, int depth) {
            if (newNode.weight == 0.0) {
                return;
            }

            if (node != null) {
                addNode2(node, position, depth);
                node = null;
            }

            for (int d = 0; d < 3; d++) {
                position[d] = (weight * position[d] + newNode.weight * newPos[d]) / (weight + newNode.weight);
            }
            weight += newNode.weight;

            addNode2(newNode, newPos, depth);
        }

        /**
         * Adds a graph node to the octtree, without changing the position and
         * weight of the root.
         *
         * @param newNode graph node
         * @param newPos position of the graph node
         * @param depth depth of this tree node in the octtree
         */
        protected void addNode2(Node newNode, double[] newPos, int depth) {
            if (depth == MAX_DEPTH) {
                if (children.length == childCount) {
                    OctTree[] oldChildren = children;
                    children = new OctTree[2 * children.length];
                    System.arraycopy(oldChildren, 0, children, 0, oldChildren.length);
                }
                children[childCount++] = new OctTree(newNode, newPos, newPos, newPos);
                return;
            }

            int childIndex = 0;
            for (int d = 0; d < 3; d++) {
                if (newPos[d] > (minPos[d] + maxPos[d]) / 2) {
                    childIndex += 1 << d;
                }
            }

            if (children[childIndex] == null) {
                double[] newMinPos = new double[3];
                double[] newMaxPos = new double[3];
                for (int d = 0; d < 3; d++) {
                    if ((childIndex & 1 << d) == 0) {
                        newMinPos[d] = minPos[d];
                        newMaxPos[d] = (minPos[d] + maxPos[d]) / 2;
                    } else {
                        newMinPos[d] = (minPos[d] + maxPos[d]) / 2;
                        newMaxPos[d] = maxPos[d];
                    }
                }

                childCount++;
                children[childIndex] = new OctTree(newNode, newPos, newMinPos, newMaxPos);
            } else {
                children[childIndex].addNode(newNode, newPos, depth + 1);
            }
        }

        /**
         * Removes a graph node from the octtree.
         *
         * @param oldNode graph node
         * @param oldPos position of the graph node
         * @param depth current depth
         */
        public void removeNode(Node oldNode, double[] oldPos, int depth) {
            if (oldNode.weight == 0.0) {
                return;
            }

            if (weight <= oldNode.weight) {
                weight = 0.0;
                node = null;
                for (int i = 0; i < children.length; i++) {
                    children[i] = null;
                }
                childCount = 0;
                return;
            }

            for (int d = 0; d < 3; d++) {
                position[d] = (weight * position[d] - oldNode.weight * oldPos[d]) / (weight - oldNode.weight);
            }
            weight -= oldNode.weight;

            if (depth == MAX_DEPTH) {
                int childIndex = 0;
                while (children[childIndex].node != oldNode) {
                    childIndex++;
                }
                childCount--;
                for (int i = childIndex; i < childCount; i++) {
                    children[i] = children[i + 1];
                }
                children[childCount] = null;
            } else {
                int childIndex = 0;
                for (int d = 0; d < 3; d++) {
                    if (oldPos[d] > (minPos[d] + maxPos[d]) / 2) {
                        childIndex += 1 << d;
                    }
                }
                children[childIndex].removeNode(oldNode, oldPos, depth + 1);
                if (children[childIndex].weight == 0.0) {
                    children[childIndex] = null;
                    childCount--;
                }
            }
        }

        /**
         * Returns the maximum extension of the octtree.
         *
         * @return maximum over all dimensions of the extension of the octtree
         */
        public double width() {
            double width = 0.0;
            for (int d = 0; d < 3; d++) {
                if (maxPos[d] - minPos[d] > width) {
                    width = maxPos[d] - minPos[d];
                }
            }
            return width;
        }

        /**
         * Returns the height of the octtree.
         *
         * @return height of the octtree
         */
        public int getHeight() {
            int height = -1;
            for (OctTree child : children) {
                if (child != null) {
                    height = Math.max(height, child.getHeight());
                }
            }
            return height + 1;
        }
    }

}
