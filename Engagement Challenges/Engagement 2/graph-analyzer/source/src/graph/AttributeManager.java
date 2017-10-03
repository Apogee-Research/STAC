package graph;

import java.util.Hashtable;

/**
 * Arbitrate access to attributes in Elements for "hash once" functionality.
 * Hash once means that there is a global hash table which contains package
 * names. This is so a package does not need to perform an expensive hashing
 * function in the middle of an inner loop.
 *
 * @see java.util.Hashtable
 * @see Element
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class AttributeManager {

    /**
     * A constant which designates that a particular package does not yet have a
     * slot in the attribute table.
     */
    public static int NO_INDEX = -1;

    private static int DEFAULT_NUM_PKG = 5;
    private static Hashtable s_hash = new Hashtable(DEFAULT_NUM_PKG);
    private static int s_numattr = 0;
    private static boolean s_dirty = false;

    /**
     * Gets the index of the attribute named by <i>packageName</i>. If
     * <i>packageName</i> doesn't yet have an index, it returns the constant
     * <b>NO_INDEX</b>.
     */
    public static int queryIndex(String packageName) {
        Integer i;
        if ((i = (Integer) s_hash.get(packageName)) != null) {
            return i.intValue();
        } else {
            return NO_INDEX;
        }
    }

    /**
     * Gets the index of the attribute named by <i>packageName</i>. If
     * <i>packageName</i> doesn't yet have an index, it creates a new slot for
     * it.
     */
    public static int getIndex(String packageName) {
        int i;
        if ((i = queryIndex(packageName)) != NO_INDEX) {
            return i;
        } else {
            Integer ind = new Integer(s_numattr++);
            if ((s_numattr % DEFAULT_NUM_PKG) == 0) {
                //we've just gotten one more than all
                //our nodes can handle
                s_dirty = true;
            }
            s_hash.put(packageName, ind);
            return ind.intValue();
        }
    }

    /**
     * @return	The number of attribute slots that have currently been reserved.
     */
    public static int getNumAttr() {
        return s_numattr;
    }

    /**
     * Obsolete function.
     */
    public static boolean getDirty() {
        return s_dirty;
    }
}
