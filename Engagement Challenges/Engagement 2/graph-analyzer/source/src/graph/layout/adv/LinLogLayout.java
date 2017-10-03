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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * LinLogLayout is a simple program for computing graph layouts (positions of
 * the nodes of a graph in two- or three-dimensional space) and graph
 * clusterings for visualization and knowledge discovery. It reads a graph from
 * a file, computes a layout and a clustering, writes the layout and the
 * clustering to a file, and displays them in a dialog. LinLogLayout can be used
 * to identify groups of densely connected nodes in graphs, like groups of
 * friends or collaborators in social networks, related documents in hyperlink
 * structures (e.g. web graphs), cohesive subsystems in software models, etc.
 * With a change of a parameter in the <code>main</code> method, it can also
 * compute classical "nice" (i.e. readable) force-directed layouts. The program
 * is primarily intended as a demo for the use of its core layouter and
 * clusterer classes <code>MinimizerBarnesHut</code>,
 * <code>MinimizerClassic</code>, and <code>OptimizerModularity</code>. While
 * <code>MinimizerBarnesHut</code> is faster, <code>MinimizerClassic</code> is
 * simpler and not limited to a maximum of three dimensions.
 *
 * @author Andreas Noack (an@informatik.tu-cottbus.de)
 * @version 13.11.2008
 */
public class LinLogLayout {

    /**
     * Reads and returns a graph from the specified file. The graph is returned
     * as a nested map: Each source node of an edge is mapped to a map
     * representing its outgoing edges. This map maps each target node of the
     * outgoing edges to the edge weight (the weight of the edge from the source
     * node to the target node). Schematically, source -> target -> edge weight.
     *
     * @param filename name of the file to read from.
     * @return read graph.
     */
    private static Map<String, Map<String, Double>> readGraph(String filename) {
        Map<String, Map<String, Double>> result = new HashMap<String, Map<String, Double>>();
        try {
            BufferedReader file = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = file.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                if (!st.hasMoreTokens()) {
                    continue;
                }
                String source = st.nextToken();
                String target = st.nextToken();
                double weight = st.hasMoreTokens() ? Double.parseDouble(st.nextToken()) : 1.0f;
                if (result.get(source) == null) {
                    result.put(source, new HashMap<String, Double>());
                }
                result.get(source).put(target, weight);
            }
            file.close();
        } catch (IOException e) {
            System.err.println("Exception while reading the graph:");
            System.err.println(e);
            System.exit(1);
        }
        return result;
    }

    /**
     * Returns a symmetric version of the given graph. A graph is symmetric if
     * and only if for each pair of nodes, the weight of the edge from the first
     * to the second node equals the weight of the edge from the second to the
     * first node. Here the symmetric version is obtained by adding to each edge
     * weight the weight of the inverse edge.
     *
     * @param graph possibly unsymmetric graph.
     * @return symmetric version of the given graph.
     */
    private static Map<String, Map<String, Double>> makeSymmetricGraph(Map<String, Map<String, Double>> graph) {
        Map<String, Map<String, Double>> result = new HashMap<String, Map<String, Double>>();
        for (String source : graph.keySet()) {
            for (String target : graph.get(source).keySet()) {
                double weight = graph.get(source).get(target);
                double revWeight = 0.0f;
                if (graph.get(target) != null && graph.get(target).get(source) != null) {
                    revWeight = graph.get(target).get(source);
                }
                if (result.get(source) == null) {
                    result.put(source, new HashMap<String, Double>());
                }
                result.get(source).put(target, weight + revWeight);
                if (result.get(target) == null) {
                    result.put(target, new HashMap<String, Double>());
                }
                result.get(target).put(source, weight + revWeight);
            }
        }
        return result;
    }

    /**
     * Construct a map from node names to nodes for a given graph, where the
     * weight of each node is set to its degree, i.e. the total weight of its
     * edges.
     *
     * @param graph the graph.
     * @return map from each node names to nodes.
     */
    private static Map<String, Node> makeNodes(Map<String, Map<String, Double>> graph) {
        Map<String, Node> result = new HashMap<String, Node>();
        for (String nodeName : graph.keySet()) {
            double nodeWeight = 0.0;
            for (double edgeWeight : graph.get(nodeName).values()) {
                nodeWeight += edgeWeight;
            }
            result.put(nodeName, new Node(nodeName, nodeWeight));
        }
        return result;
    }

    /**
     * Converts a given graph into a list of edges.
     *
     * @param graph the graph.
     * @param nameToNode map from node names to nodes.
     * @return the given graph as list of edges.
     */
    private static List<Edge> makeEdges(Map<String, Map<String, Double>> graph,
            Map<String, Node> nameToNode) {
        List<Edge> result = new ArrayList<Edge>();
        for (String sourceName : graph.keySet()) {
            for (String targetName : graph.get(sourceName).keySet()) {
                Node sourceNode = nameToNode.get(sourceName);
                Node targetNode = nameToNode.get(targetName);
                double weight = graph.get(sourceName).get(targetName);
                result.add(new Edge(sourceNode, targetNode, weight));
            }
        }
        return result;
    }

    /**
     * Returns, for each node in a given list, a random initial position in two-
     * or three-dimensional space.
     *
     * @param nodes node list.
     * @param is3d initialize 3 (instead of 2) dimension with random numbers.
     * @return map from each node to a random initial positions.
     */
    public static Map<Node, double[]> makeInitialPositions(List<Node> nodes, boolean is3d) {
        Map<Node, double[]> result = new HashMap<Node, double[]>();
        for (Node node : nodes) {
            double[] position = {Math.random() - 0.5,
                Math.random() - 0.5,
                is3d ? Math.random() - 0.5 : 0.0};
            result.put(node, position);
        }
        return result;
    }

    /**
     * Writes a given layout and clustering into the specified file.
     *
     * @param nodeToPosition map from each node to its layout position.
     * @param nodeToPosition map from each node to its cluster.
     * @param filename name of the file to write into.
     */
    private static void writePositions(Map<Node, double[]> nodeToPosition,
            Map<Node, Integer> nodeToCluster, String filename) {
        try {
            BufferedWriter file = new BufferedWriter(new FileWriter(filename));
            for (Node node : nodeToPosition.keySet()) {
                double[] position = nodeToPosition.get(node);
                int cluster = nodeToCluster.get(node);
                file.write(node.name + " " + position[0] + " " + position[1]
                        + " " + position[2] + " " + cluster);
                file.write("\n");
            }
            file.close();
        } catch (IOException e) {
            System.err.println("Exception while writing the graph:");
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * Reads a graph from a specified input file, computes a layout and a
     * clustering, writes the layout and the clustering into a specified output
     * file, and displays them in a dialog.
     *
     * @param args number of dimensions, name of the input file and of the
     * output file. If <code>args.length != 3</code>, the method outputs a help
     * message.
     */
    public static void main(final String[] args) {
        if (args.length != 3 || (!args[0].equals("2") && !args[0].equals("3"))) {
            org.graph.commons.logging.LogFactory.getLog(null).info(
                    "Usage: java LinLogLayout <dim> <inputfile> <outputfile>\n"
                    + "Computes a <dim>-dimensional layout and a clustering for the graph\n"
                    + "in <inputfile>, writes the layout and the clustering into <outputfile>,\n"
                    + "and displays (the first 2 dimensions of) the layout and the clustering.\n"
                    + "<dim> must be 2 or 3.\n\n"
                    + "Input file format:\n"
                    + "Each line represents an edge and has the format:\n"
                    + "<source> <target> <nonnegative real weight>\n"
                    + "The weight is optional, the default value is 1.0.\n\n"
                    + "Output file format:\n"
                    + "<node> <x-coordinate> <y-coordinate> <z-coordinate (0.0 for 2D)> <cluster>"
            );
            System.exit(0);
        }

        Map<String, Map<String, Double>> graph = readGraph(args[1]);
        graph = makeSymmetricGraph(graph);
        Map<String, Node> nameToNode = makeNodes(graph);
        List<Node> nodes = new ArrayList<Node>(nameToNode.values());
        List<Edge> edges = makeEdges(graph, nameToNode);
        Map<Node, double[]> nodeToPosition = makeInitialPositions(nodes, args[0].equals("3"));
		// see class MinimizerBarnesHut for a description of the parameters;
        // for classical "nice" layout (uniformly distributed nodes), use
        //new MinimizerBarnesHut(nodes, edges, -1.0, 2.0, 0.05).minimizeEnergy(nodeToPosition, 100);
        new MinimizerBarnesHut(nodes, edges, 0.0, 1.0, 0.05).minimizeEnergy(nodeToPosition, 100);
        // see class OptimizerModularity for a description of the parameters
        Map<Node, Integer> nodeToCluster
                = new OptimizerModularity().execute(nodes, edges, false);
        writePositions(nodeToPosition, nodeToCluster, args[2]);
        (new GraphFrame(nodeToPosition, nodeToCluster)).setVisible(true);
    }

}
