package graph.layout;

import graph.*;
import graph.editor.*;
import graph.cluster.*;
import graph.rep.*;
import java.awt.*;
import java.util.*;

/**
 * The force-directed placement algorithm which operates on the graph.
 *
 * This algorithm is based on code from Elan Amir (elan@cs.berkeley.edu) which
 * in turn was based on Fruchterman and Reingold,"Graph Drawing by
 * Force-Directed Placement", Univ of Illinois Urbana-Champaign, Report No.
 * UIUCDS-R-90-1609.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class ForceLayout implements Action, Painter {

    public static int s_forceIndex = AttributeManager.NO_INDEX;
    float m_temp = .05f * 100f;
    int m_width;
    int m_height;
    int m_border;

    public ForceLayout(int width, int height, int border) {
        m_width = width;
        m_height = height;
        m_border = border;
        if (s_forceIndex == AttributeManager.NO_INDEX) {
            s_forceIndex = AttributeManager.getIndex("Force");
        }
    }

    /**
     * Calculate the attractive force given by the expression (x*x/k)
     */
    static double attract(double x, double k) {
        return x * x / k;
    }

    /**
     * Calculate the repulsive force given by the expression (k*k/x)
     */
    static double repel(double x, double k) {
        return k * k / x;
    }

    /**
     * Cool the temperature at which the algorithm operates. The lowest
     * temperature possible is .001f.
     */
    void cool() {
        if (m_temp > 0.001) {
            m_temp *= 0.95f;
        } else {
            m_temp = 0.001f;
        }
    }

    public void apply(Graph g) {
        init(g);
        for (int i = 0; i < 10; i++) {
            step(g);
        }
        finish(g);
    }

    public static final int NUM_DUMMY = 8;

    /**
     * Place a string of dummy nodes
     */
    public void initDummy(Graph g, Point p1, Point p2) {
        for (int i = 0; i < NUM_DUMMY; i++) {
            DummyNode d = new DummyNode();
            double t = ((double) i) / ((double) (NUM_DUMMY - 1));
            d.x = (int) ((1.0f - t) * p1.x + t * p2.x);
            d.y = (int) ((1.0f - t) * p1.y + t * p2.y);
//			org.graph.commons.logging.LogFactory.getLog(null).info("Adding: " + d.x + ", " + d.y);
            g.add(d);
        }
    }

    public void init(Graph g) {
        //first thing, partition this puppy
        (new GreedyCluster()).apply(g);

        //add a bunch of dummy nodes to the corners/edges of
        //the graph
        int w = m_width;
        int h = m_height;
        int b = m_border;

        initDummy(g, new Point(b, b), new Point(w - b, b));
        initDummy(g, new Point(b, b), new Point(b, h - b));
        initDummy(g, new Point(w - b, b), new Point(w - b, h - b));
        initDummy(g, new Point(b, h - b), new Point(w - b, h - b));

        //initialize the force attributes
        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            Node n = (Node) e.nextElement();
            ForceAttr a = (ForceAttr) n.getAttr(s_forceIndex);
            if (a == null) {
                a = new ForceAttr();
                if (n instanceof DummyNode) {
                    a.peg = true;
                }
                n.setAttr(s_forceIndex, a);
            }
        }
    }

    public void finish(Graph g) {
        for (Enumeration e = g.nodes.elements(); e.hasMoreElements();) {
            Node n = (Node) e.nextElement();
            if (n instanceof DummyNode) {
                g.delete(n);
            }
        }
    }

    public Hashtable m_hash = new Hashtable();
    public Color m_cool = new Color(30, 30, 180);
    public Color m_cold = new Color(50, 50, 220);

    public void paint(Graphics g) {
        Enumeration elts = m_hash.elements();
        while (elts.hasMoreElements()) {
            MovementTrail t = (MovementTrail) elts.nextElement();
            boolean cold = (t.prevx != t.prevx2) && (t.prevy != t.prevy2);
            g.setColor(m_cool);
            g.drawLine((int) t.x, (int) t.y, (int) t.prevx, (int) t.prevy);
            if (cold) {
                g.setColor(m_cold);
                g.drawLine((int) t.prevx, (int) t.prevy, (int) t.prevx2, (int) t.prevy2);
            }
            g.setColor(m_cool);
            g.fillRect((int) t.prevx - 1, (int) t.prevy - 1, 2, 2);
            if (cold) {
                g.setColor(m_cold);
                g.fillRect((int) t.prevx2 - 1, (int) t.prevy2 - 1, 2, 2);
            }
        }
    }

    /**
     * Apply the layout algorithm to the contents of a graph. Cool after
     * application so that next time the algorithm has a lower impact on the
     * layout (and will eventually settle into some local minimum)
     * .<P>
     *
     * The force layout has three steps:
     * <OL>
     * <LI>Spread the nodes out based on their proximity.</LI>
     * <LI>Expand and contract edges between nodes based on the edge
     * weights</LI>
     * <LI>Limit the node displacement to the edge of the display</LI>
     * </OL>
     */
    public void step(Graph g) {
        Node n1, n2;
        Edge e;
        ForceAttr a1, a2;

        double dx, dy, mag, meanMass, f, minn;

        // 1. Calculate repulsive forces between all nodes
        //    based on position.  This is so that the following
        //    configuration works out.
        //
        //		A              A
        //	   / \     =/=>    |\
        //	  B   C            BC
        for (int i = 0; i < g.nodes.size(); i++) {
            n1 = (Node) g.nodes.elementAt(i);
            a1 = (ForceAttr) n1.getAttr(s_forceIndex);
            if (a1.peg) {
                continue;
            }

            a1.embX = .5f;
            a1.embY = .5f;
            for (int j = 0; j < g.nodes.size(); j++) {
                if (i == j) {
                    continue;
                }
                n2 = (Node) g.nodes.elementAt(j);
                a2 = (ForceAttr) n2.getAttr(s_forceIndex);
                dx = n1.x - n2.x;
                dy = n1.y - n2.y;
                if (dx == 0 && dy == 0) {
                    dx = dy = 0.0001f;
                }
                mag = Math.sqrt(dx * dx + dy * dy);
                meanMass = .5f * (a1.mass + a2.mass);
                f = repel(mag, meanMass);
                a1.embX += (dx / mag * f);
                a1.embY += (dy / mag * f);
            }
        }

        // 2. Calculate attractive forces between neighbor
        //    nodes connected by edges.  We only do this
        //    on the "out" edges for each node because the
        //    "in" edges will be the "out" edges for another
        //    node.  In this way we are sure to only count
        //    each edge once.
        for (int i = 0; i < g.nodes.size(); i++) {
            n1 = (Node) g.nodes.elementAt(i);
            a1 = (ForceAttr) n1.getAttr(s_forceIndex);

            for (int j = 0; j < n1.out.size(); j++) {
                e = (Edge) n1.out.elementAt(j);
                n2 = e.head;
                a2 = (ForceAttr) n2.getAttr(s_forceIndex);
                dx = n1.x - n2.x;
                dy = n1.y - n2.y;
                mag = Math.sqrt(dx * dx + dy * dy);
                if (mag != 0) {
                    f = attract(mag, (float) e.weight); //edges k???
                } else {
                    f = 1000.0f * mag * mag;
                }
                if (!a1.peg) {
                    a1.embX -= dx / mag * f;
                    a1.embY -= dy / mag * f;
                }
                if (!a2.peg) {
                    a2.embX += dx / mag * f;
                    a2.embY += dy / mag * f;
                }
            }
        }

        // 3. Limit the maximum displacement to the temperature temp;
        //    and then prevent from being displaced outside frame.
        for (int i = 0; i < g.nodes.size(); i++) {
            n1 = (Node) g.nodes.elementAt(i);
            a1 = (ForceAttr) n1.getAttr(s_forceIndex);
            if (a1.peg) {
                continue;
            }
            dx = a1.embX - n1.x;
            dy = a1.embY - n1.y;
            mag = (float) Math.sqrt(dx * dx + dy * dy);

            //for debugging
            MovementTrail trail = (MovementTrail) m_hash.get(n1);
            if (trail != null) {
                trail.prevx2 = trail.prevx;
                trail.prevy2 = trail.prevy;
                trail.prevx = trail.x;
                trail.prevy = trail.y;
            } else {
                trail = new MovementTrail();
            }

            if (mag != 0) {
                minn = Math.min(mag, 5 * m_temp/*EmbedCanvas.IMG_X*/);

                org.graph.commons.logging.LogFactory.getLog(null).info("Displacing (" + dx / mag * minn + ", " + dy / mag * minn);
                n1.x += dx / mag * minn;
                n1.y += dy / mag * minn;

            }

            //enforce display boundary
            n1.x = Math.min(Math.max(m_border, n1.x),
                    m_width - m_border);
            n1.y = Math.min(Math.max(m_border, n1.y),
                    m_height - m_border);

            trail.x = n1.x;
            trail.y = n1.y;

            m_hash.put(n1, trail);
        }

        // 4. Cool the temperature
        cool();
    }
}

/**
 * Annotation information for nodes to help draw the history of their movement
 * over the course of the algorithm.
 */
class MovementTrail {

    double x = -1;
    double y = -1;
    double prevx = -1;
    double prevy = -1;
    double prevx2 = -1;
    double prevy2 = -1;
}
