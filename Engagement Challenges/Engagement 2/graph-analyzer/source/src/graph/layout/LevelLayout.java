package graph.layout;

import graph.*;
import java.util.*;

/**
 * A force-directed placement algorithm originally implemented by �lfar
 * Erlingsson at Cornell/RPI and modified to fit into this system.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
class LevelInfo {

    boolean hasDummies = false;
    double barycenter;
    int level = -1;
    int useage = 1000; //XXX
    Node metaRoot = null;
    boolean mark = false;
}

public class LevelLayout implements Action {

    double m_l = 500;
    int m_maxLevel = -1;
    Vector m_levels[] = null;
    public static int s_levelIndex = AttributeManager.NO_INDEX;

    public void apply(Graph g) {
        init(g);
        makeLevels(g);
        placeNodes();	//XXX
    } //XXX

    public void step(Graph g) {
    }

    public void finish(Graph g) {
    } //XXX

    public LevelLayout() {
        if (s_levelIndex == AttributeManager.NO_INDEX) {
            s_levelIndex = AttributeManager.getIndex("Level");
        }
    }

    public void init(Graph g) {
        addLevelAttrs(g);
        preprocess(g);
        addDummies(g);
    }

    /**
     * Add a LevelInfo to the graph and each node in the graph.
     */
    void addLevelAttrs(Graph g) {
        LevelInfo linfo = (LevelInfo) g.getAttr(s_levelIndex);
        if (linfo == null) {
            linfo = new LevelInfo();
            g.setAttr(s_levelIndex, linfo);
        }

        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            Node n = (Node) e.nextElement();
            LevelInfo inf = (LevelInfo) n.getAttr(s_levelIndex);
            if (inf == null) {
                inf = new LevelInfo();
                n.setAttr(s_levelIndex, inf);
            }
        }
    }

    /**
     * Add this node and all of its parent nodes to the levels array.
     */
    void initialOrderNodes(Node n) {
        setMark(n, true);
        for (Enumeration ins = n.in.elements(); ins.hasMoreElements();) {
            Edge e = (Edge) ins.nextElement();
            if (!getMark(e.tail)) {
                initialOrderNodes(e.tail);
            }
        }
        m_levels[getLevel(n)].addElement(n);
    }

    /**
     * Add and remove dummy nodes between nodes between levels that are far
     * apart.
     */
    void addDummies(Graph g) {
        LevelInfo linfo = (LevelInfo) g.getAttr(s_levelIndex);

        if (linfo.hasDummies) {
            return;
        }
        linfo.hasDummies = true;

        for (Enumeration nodes = g.nodes.elements(); nodes.hasMoreElements();) {
            Node to = (Node) nodes.nextElement();
            if (to instanceof DummyNode) {
                continue;
            }
            LevelInfo nlinfo = (LevelInfo) to.getAttr(s_levelIndex);

            for (Enumeration in = to.in.elements(); in.hasMoreElements();) {
                Edge e = (Edge) in.nextElement();
                if (e.tail instanceof DummyNode) {
                    continue;
                }
                while (getLevel(to) > getLevel(e.tail) + 1) {
                    //dummy gets stuck between e.tail and e.head
                    Node dummy = new DummyNode();
                    LevelInfo dumInfo = new LevelInfo();
                    dumInfo.level = getLevel(e.tail) + 1;
                    dummy.setAttr(s_levelIndex, dumInfo);
                    g.add(dummy);
                    e.swapHead(dummy);
                    //e is reassigned to make the while() work
                    try {
                        e = dummy.attach(to);
                    } catch (Exception ex) {
                        org.graph.commons.logging.LogFactory.getLog(null).info(ex.toString());
                        System.exit(0);//XXX
                    }
                }
            }
        }
    }

    /**
     * Get the level of <i>n</i> in the graph. Requires that all nodes have
     * LevelInfo attributes.
     */
    void makeLevels(Graph g) {
        m_maxLevel = -1;
        Node maxNode = null;
        int level;
        //find the topmost node
        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            Node n = (Node) e.nextElement();
            if ((level = getLevel(n)) > m_maxLevel) {
                m_maxLevel = level;
                maxNode = n;
            }
        }

        //create some buckets to store the nodes
        m_levels = new Vector[m_maxLevel + 1];
        for (int i = 0; i < m_maxLevel + 1; i++) {
            m_levels[i] = new Vector();
        }

        //clear all the nodes
        markAll(g, false);

        //initialize
        initialOrderNodes(maxNode);

        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            Node n = (Node) e.nextElement();
            if (!getMark(n)) {
                initialOrderNodes(n);
            }
        }

    }

    /**
     * Place all the nodes on a level. Invoke this after the nodes have been
     * assigned to their levels, and have been sorted properly.
     */
    void placeLevel(double l, double y, Vector nodes) {
        double xstep = l / (nodes.size() + 1);
        int i = 0;
        for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
            Node n = (Node) e.nextElement();
            n.x = xstep * (++i);
            n.y = y;
        }
    }

    /**
     * Invoke <i>placeLevel</i> on each level in the graph.
     */
    void placeNodes() {
        double ystep = m_l / (m_maxLevel + 1);
        double y = 0.0;
        for (int i = 0; i <= m_maxLevel; i++) {
            Vector nodes = m_levels[i];
            placeLevel(m_l, y, nodes);
            y += ystep;
        }
    }

    /**
     * Do insertion sort on the level based on the barycenters, then reorder
     */
    protected final void sortLevel(Vector nodes) {
        int len = nodes.size();
        for (int i = 1; i < len; i++) {
            Node n1 = (Node) nodes.elementAt(i);
            double barycent = barycenter(n1);
            int j;
            for (j = i; j > 0; j--) {
                Node n2 = (Node) nodes.elementAt(j - 1);
                if (barycent >= barycenter(n2)) {
                    break;
                }
                nodes.setElementAt(n2, j);
            }
            nodes.setElementAt(n1, j);
        }
    }

    protected final void orderLevel(Vector nodes, double l, double y,
            boolean doin, boolean doout) {
        int levelcnt = nodes.size();
        for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
            Node n = (Node) e.nextElement();
            computeBarycenter(n, doin, doout);
        }
        sortLevel(nodes);
        placeLevel(l, y, nodes);
    }

    // Do downwards barycentering on first pass, upwards on second, then average
    protected synchronized final void orderNodes(double l, int op) {
        boolean doup = ((op & 0x1) == 1);
        boolean doin = (op > 5 || !doup);
        boolean doout = (op > 5 || doup);
        double ystep = (m_maxLevel > 0) ? (m_l / m_maxLevel) : 0.0;
        if (doup) {
            double y = 0.0;
            for (int i = 0; i <= m_maxLevel; ++i) {         // Going upwards
                Vector nodes = m_levels[i];
                orderLevel(nodes, l, y, doin, doout);
                y += ystep;
            }
        } else {
            double y = l;
            for (int i = m_maxLevel; i >= 0; --i) {         // Going downwards
                Vector nodes = m_levels[i];
                orderLevel(nodes, l, y, doin, doout);
                y -= ystep;
            }
        }
    }

    protected final void straightenDummy(Node n) {
        Node tail = ((Edge) n.in.firstElement()).tail;
        Node head = ((Edge) n.in.firstElement()).head;
        double avg = (n.x + tail.x + head.x) / 3;
        n.x = avg;
    }

    private final int xmarginSize = 10;

    protected synchronized final void straightenLayout(double l) {
        double ystep = l / (m_maxLevel + 1);
        double y = 0.0;
        for (int i = 0; i <= m_maxLevel; i++) {
            Vector nodes = m_levels[i];
            for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
                Node n = (Node) e.nextElement();
                if (n instanceof DummyNode) {
                    straightenDummy(n);
                }
            }

            for (int j = 1; j < nodes.size(); j++) {
                Node n = (Node) nodes.elementAt(j);
                Node prev = (Node) nodes.elementAt(j - 1);
                double prevright = prev.x + prev.w / 2 + xmarginSize;
                double thisleft = n.x - n.w / 2 - xmarginSize;
                double overlap = prevright - thisleft;
                if (overlap > 0) {
                    prev.x = prev.x - overlap / 2;
                    n.x = n.x + overlap / 2;
                }
                n.y = y;
            }
            y += ystep;
        }
    }

    /*  XXX

     protected int _operation = 0;
     protected final int _Order = 100;
     public final void Embed() {
     double L = _bb.globals.L();
     _bb.setArea( 0, 0, L, L );
     if( _operation < _Order ) {
     orderNodes( L, _operation );
     }
     else {
     straightenLayout( L );
     }
     _bb.Update();
     ++_operation;
     _bb.globals.Temp( (double)_operation );
     }

     */
    //==================================================================
    // UTILITY FUNCTIONS HERE
    //==================================================================
    void computeBarycenter(Node n, boolean doin, boolean doout) {
        double insum = 0.0;
        int insize = 0;
        int outsize = 0;

        if (doin) {
            insize = n.in.size();
            for (Enumeration e = n.in.elements(); e.hasMoreElements();) {
                insum += ((Edge) e.nextElement()).tail.x;
            }
            if (insize == 0) {
                insize = 1;
                insum = n.x;
            }
        }

        double outsum = 0.0;
        if (doout) {
            outsize = n.out.size();
            for (Enumeration e = n.out.elements(); e.hasMoreElements();) {
                outsum += ((Edge) e.nextElement()).head.x;
            }
            if (outsize == 0) {
                outsize = 1;
                outsum = n.x;
            }
        }

        double barycenter;
        if (doin && doout) {
            barycenter = (insum + outsum) / (insize + outsize);
        } else if (doin) {
            barycenter = insum / insize;
        } else if (doout) {
            barycenter = outsum / outsize;
        } else {
            barycenter = n.x;
        }

        LevelInfo info = (LevelInfo) n.getAttr(s_levelIndex);
        info.barycenter = barycenter;
    }

    double barycenter(Node n) {
        LevelInfo info = (LevelInfo) n.getAttr(s_levelIndex);
        return info.barycenter;
    }

    /**
     * Mark all the nodes in the graph as <i>val</i>. Requires that all nodes
     * have LevelInfo attributes.
     *
     * @param val	The value to set the mark.
     */
    void markAll(Graph g, boolean val) {
        setMark(g, val);
        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            setMark((Node) e.nextElement(), false);
        }
    }

    /**
     * Mark a node as <i>val</i>. Requires that node have LevelInfo attributes.
     */
    void setMark(Node n, boolean val) {
        LevelInfo linfo = (LevelInfo) n.getAttr(s_levelIndex);
        linfo.mark = val;
    }

    /**
     * Get the value of <i>n</i>'s mark. Requires that node has LevelInfo
     * attributes.
     */
    boolean getMark(Node n) {
        LevelInfo linfo = (LevelInfo) n.getAttr(s_levelIndex);
        return linfo.mark;
    }

    /**
     * Get the level of <i>n</i> in the graph. Requires that node has LevelInfo
     * attribute.
     */
    int getLevel(Node n) {
        LevelInfo inf = (LevelInfo) n.getAttr(s_levelIndex);
        return inf.level;
    }

    /**
     * Set the level of <i>n</i> in the graph. Requires that node has LevelInfo
     * attribute.
     */
    void setLevel(Node n, int l) {
        LevelInfo inf = (LevelInfo) n.getAttr(s_levelIndex);
        inf.level = l;
    }

    /**
     * Get the level of <i>n</i> in the graph. Requires that node has LevelInfo
     * attribute.
     */
    int getUseage(Node n) {
        LevelInfo inf = (LevelInfo) n.getAttr(s_levelIndex);
        return inf.useage;
    }

    /**
     * Set the level of <i>n</i> in the graph. Requires that node has LevelInfo
     * attribute.
     */
    void setUseage(Node n, int val) {
        LevelInfo inf = (LevelInfo) n.getAttr(s_levelIndex);
        inf.useage = val;
    }

    void preprocess(Graph g) {
        markAll(g, false);
        Node meta = makeMeta(g);
        Vector topo = new Vector();
        topoSort(meta, topo);
        removeMeta(g);

        int maxlevel = 0;
        int level = 0;
        for (Enumeration e = topo.elements(); e.hasMoreElements();) {
            Node n1 = (Node) e.nextElement();
            for (Enumeration ins = n1.in.elements(); ins.hasMoreElements();) {
                Node n2 = ((Edge) ins.nextElement()).tail;
                if (getLevel(n2) > level) {
                    level = getLevel(n2);
                }
            }
            setLevel(n1, level + 1);
            if (level + 1 > maxlevel) {
                maxlevel = level + 1;
            }
        }

        for (int i = topo.size() - 1; i >= 0; i--) {
            Node n1 = (Node) topo.elementAt(i);
            int minUseage = maxlevel;

            int outedgecnt = n1.out.size();
            if (n1.out.size() == 0) {
                minUseage = getLevel(n1);
            }
            for (Enumeration e = n1.out.elements(); e.hasMoreElements();) {
                Node n2 = ((Edge) e.nextElement()).tail;
                if ((getUseage(n2) - 1) < minUseage) {
                    minUseage = getUseage(n2) - 1;
                }
            }
            setUseage(n1, minUseage);
        }

        for (Enumeration e = topo.elements(); e.hasMoreElements();) {
            Node n = (Node) e.nextElement();
            setLevel(n, getUseage(n));
        }
    }

    void topoSort(Node n, Vector topo) {
        setMark(n, true);
        for (Enumeration e = n.in.elements(); e.hasMoreElements();) {
            Node n2 = ((Edge) e.nextElement()).tail;
            if (!getMark(n2)) {
                topoSort(n2, topo);
            }
        }
        topo.addElement(n);
    }

    Node makeMeta(Graph g) {
        Node meta = new Node();
        meta.name = "meta";
        meta.setAttr(s_levelIndex, new LevelInfo());
        LevelInfo inf = (LevelInfo) g.getAttr(s_levelIndex);
        inf.metaRoot = meta;

        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            Node n2 = (Node) e.nextElement();
            try {
                n2.attach(meta);
            } catch (Exception ex) {
                org.graph.commons.logging.LogFactory.getLog(null).info(ex.toString());
                System.exit(0);
            }
        }

        g.nodes.addElement(meta);
        return meta;
    }

    void removeMeta(Graph g) {
        LevelInfo inf = (LevelInfo) g.getAttr(s_levelIndex);
        g.delete(inf.metaRoot);
        inf.metaRoot = null;
    }
}
