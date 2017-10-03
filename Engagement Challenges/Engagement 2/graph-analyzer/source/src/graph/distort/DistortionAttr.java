package graph.distort;

/**
 * Position information which helps distortion algorithms operate on a graph.
 *
 * @author Michael Shilman <michaels@eecs.berkeley.edu>
 * @version $Id$
 */
public class DistortionAttr {

    public int prevPos[] = new int[2];
    public int prevK = 0;
}
