package graph;

/**
 * A basic graph element which can be annotated. The annotation is currently an
 * array of Object slots.
 *
 * @see AttributeManager
 * @see Node
 * @see Graph
 * @see Edge
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public abstract class Element {

    public static final int MAX_ATTR = 3;

    /**
     * The storage for the attributes.
     */
    public Object attrs[] = null;

    /**
     * Get the <i>i'th</i> attribute. Allocate the attribute array if necessary.
     *
     * @param i	The array index.
     */
    public Object getAttr(int i) {
        if (attrs == null) {
            attrs = new Object[MAX_ATTR];
        }
        return attrs[i];
    }

    /**
     * Set the <i>i'th</i> attribute. Allocate the attribute array if necessary.
     *
     * @param i	The array index.
     * @param attr	The attribute value.
     */
    public void setAttr(int i, Object attr) {
        if (attrs == null) {
            attrs = new Object[MAX_ATTR];
        }
        attrs[i] = attr;
    }
}
