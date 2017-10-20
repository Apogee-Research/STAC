package graph.dot;

import graph.*;
import graph.editor.Editor;
import java.io.*;
import java.util.*;

/**
 * Output a graph into a .DOT file format
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class DotWriter {

    public static int s_dotIndex = AttributeManager.NO_INDEX;

    public static void write(Graph g, String fname) throws IOException {
        FileOutputStream fop = null;
        File file;
        String content = "This is the text content";

        file = new File(fname);
        fop = new FileOutputStream(file);

        write(g, fop);
    }

    public static void write(Graph g, FileOutputStream fop) throws IOException {

        DataOutputStream os = new DataOutputStream(fop);
        os.writeChars("graph \"" + concat(g.name) + "\" {\n");
        Node n;
        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            n = (Node) e.nextElement();
            writeNode(n, os);
        }
        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            n = (Node) e.nextElement();
            writeEdges(n, os);
        }
        os.writeChars("}\n");

    }

    /**
     * Output a graph into a .DOT file format
     */
    public static void write(Graph g, OutputStream s)
            throws IOException {
        DataOutputStream os = new DataOutputStream(s);
        os.writeChars("graph \"" + concat(g.name) + "\" {\n");
        Node n;
        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            n = (Node) e.nextElement();
            writeNode(n, os);
        }
        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            n = (Node) e.nextElement();
            writeEdges(n, os);
        }
        os.writeChars("}\n");
    }

    public static int calls = 0;

    /**
     * Write out the properties of this node
     */
    protected static void writeNode(Node n, DataOutputStream os)
            throws IOException {
        calls++;
        if (calls > 1000000) {
            System.exit(1);
        }

        os.writeChars("\t" + concat(n.name) + " [ ");
        os.writeChars("pos=\"" + n.x + "," + n.y + "\"");
        //XXX actually get the properties
        String type = n.getType();
        org.graph.commons.logging.LogFactory.getLog(null).info("0");

        if (type != null && type.startsWith("container:")) {
            org.graph.commons.logging.LogFactory.getLog(null).info("0.1");
            String containername = type.substring("container:".length());

            //Graph subg = graphs.get(containername);
            org.graph.commons.logging.LogFactory.getLog(null).info("1");

            //write( subg, os);
        }
        org.graph.commons.logging.LogFactory.getLog(null).info("2");

        os.writeChars(" ];\n");
    }

    /**
     * Write out the outgoing edges of this node
     */
    protected static void writeEdges(Node n, DataOutputStream os)
            throws IOException {
        Edge e;
        for (int i = 0; i < n.out.size(); i++) {
            e = (Edge) n.out.elementAt(i);
            os.writeChars("\t" + concat(e.tail.name)
                    + (e.directed ? " -> " : " -- ")
                    + concat(e.head.name) + "\n");
        }
    }

    public static String concat(String s) {
        return ((s == null) ? s : s.replace(' ', '_'));
    }
}
