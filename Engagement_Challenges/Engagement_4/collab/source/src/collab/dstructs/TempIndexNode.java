/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collab.dstructs;

//package org.j4me.collections;
import collab.CollabRuntimeException;
import collab.SchedulingSandbox;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>TempIndexNode</code> objects can be combined into a tree. The tree is not a
 * special tree such as a balanced tree. It can have any number of levels and
 * each node can have any number of children.
 * <p>
 * Each tree node may have at most one parent and 0 or more children.
 * <code>TempIndexNode</code> provides operations for examining and modifying a
 * node's parent and children. A node's tree is the set of all nodes that can be
 * reached by starting at the node and following all the possible links to
 * parents and children. A node with no parent is the root of its tree; a node
 * with no children is a leaf. A tree may consist of many subtrees, each node
 * acting as the root for its own subtree.
 * <p>
 * Every <code>TempIndexNode</code> can hold a reference to one user object. It is up
 * to the developer to decide what the object is and how to use it. For example
 * in maintaining the golf course tree the user object is an
 * <code>ICourseElement</code>.
 * <p>
 * <i>This is not a thread safe class.</i> If used from multiple threads it must
 * be manually sychronized by your code.
 */
public class TempIndexNode implements Walkable {

    /**
     * This node's parent node. If this is the root of the tree then the parent
     * will be <code>null</code>.
     */
    public TempIndexNode parent;

    /**
     * An array of all this node's child nodes. The array will always exist
     * (i.e. never <code>null</code>) and be of length zero if this is a leaf
     * node.
     * <p>
     * This is an array instead of a <code>Vector</code> to favor speed of
     * accessing the children. The array takes longer on adds because of array
     * copying and management. However to get the array it can just be returned
     * instead of creating a new array from the <code>Vector</code> each time.
     * The tree is used frequently enough for reading course elements that this
     * difference makes a small impact on rendering.
     */
    private TempIndexNode[] children = new TempIndexNode[0];

    public void setChilren(TempIndexNode[] newchildren) {
        children = newchildren;
    }

    public static int lastuniqueid = 0;
    public int uniqueid;
    /**
     * Constructs a tree node object. It can become the root of a tree. Or it
     * can become a child of another node by calling the other node's
     * <code>add</code> method.
     * <p>
     * There is no user object attached to the node. Call
     * <code>setUserObject</code> to attach one.
     */
    public TempIndexNode() {
        lastuniqueid++;
        uniqueid = lastuniqueid;
        // Nothing needed.
    }

    /**
     * Constructs a tree node object. It can become the root of a tree. Or it
     * can become a child of another node by calling the other node's
     * <code>add</code> method.
     *
     * @param userObject is an object this node encapsulates. It is up to the
     * developer to maintain its type. To get the object back out call
     * <code>getUserObject</code>.
     */
    public TempIndexNode(Object userObject) {
        this();
        m_userData = userObject;
    }

    /**
     * Adds the <code>child</code> node to this container making this its
     * parent.
     *
     * @param child is the node to add to the tree as a child of
     * <code>this</code>
     *
     * @param index is the position within the children list to add the child.
     * It must be between 0 (the first child) and the total number of current
     * children (the last child). If it is negative the child will become the
     * last child.
     * @return  nothing
     */
    public TempIndexNode add(TempIndexNode child, int index) {
        //System.out.println("add " + child);
        for (int i = 0; i < children.length; i++) {
            if (children[i].equals(child)) {
                System.out.println("child collision");
                return children[i];
            }

        }
        if (this.equals(child)) {
            System.out.println("this collision");
            return this;
        }
    //System.out.println( child + "added to " + this);

        // Add the child to the list of children.
        if (index < 0 || index == children.length) // then append
        {
            TempIndexNode[] newChildren = new TempIndexNode[children.length + 1];
            System.arraycopy(children, 0, newChildren, 0, children.length);
            newChildren[children.length] = child;
            children = newChildren;
        } else if (index > children.length) {
            throw new IllegalArgumentException("Cannot add child to index " + index + ".  There are only " + children.length + " children.");
        } else // insert
        {
            TempIndexNode[] newChildren = new TempIndexNode[children.length + 1];
            if (index > 0) {
                System.arraycopy(children, 0, newChildren, 0, index);
            }
            newChildren[index] = child;
            System.arraycopy(children, index, newChildren, index + 1, children.length - index);
            children = newChildren;
        }

        // Set the parent of the child.
        child.parent = this;
        return child;
    }

    /**
     * Adds the <code>child</code> node to this container making this its
     * parent. The child is appended to the list of children as the last child.
     * @param child nothing
     * @return nothing
     */
    public TempIndexNode add(TempIndexNode child) {
        return add(child, -1);
    }

    /**
     * Removes the child at position <code>index</code> from the tree.
     *
     * @param index is the position of the child. It should be between 0 (the
     * first child) and the total number of children minus 1 (the last child).
     * @return The removed child node. This will be <code>null</code> if no
     * child exists at the specified <code>index</code>.
     */
    public TempIndexNode remove(int index) {
        if (index < 0 || index >= children.length) {
            throw new IllegalArgumentException("Cannot remove element with index " + index + " when there are " + children.length + " elements.");
        }

        // Get a handle to the node being removed.
        TempIndexNode node = children[index];
        node.parent = null;

        // Remove the child from this node.
        TempIndexNode[] newChildren = new TempIndexNode[children.length - 1];
        if (index > 0) {
            System.arraycopy(children, 0, newChildren, 0, index);
        }
        if (index != children.length - 1) {
            System.arraycopy(children, index + 1, newChildren, index, children.length - index - 1);
        }
        children = newChildren;

        return node;
    }

    /**
     * Removes this node from its parent. This node becomes the root of a
     * subtree where all of its children become first level nodes.
     * <p>
     * Calling this on the root node has no effect.
     */
    public void removeFromParent() {
        if (parent != null) {
            int position = this.index();
            parent.remove(position);
            parent = null;
        }
    }

    /**
     * Gets the parent node of this one.
     *
     * @return The parent of this node. This will return <code>null</code> if
     * this node is the root node in the tree.
     */
    public TempIndexNode getParent() {
        return parent;
    }

    /**
     * Returns if this node is the root node in the tree or not.
     *
     * @return <code>true</code> if this node is the root of the tree;
     * <code>false</code> if it has a parent.
     */
    public boolean isRoot() {
        if (parent == null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets a list of all the child nodes of this node.
     *
     * @return An array of all the child nodes. The array will be the size of
     * the number of children. A leaf node will return an empty array, not
     * <code>null</code>.
     */
    public TempIndexNode[] children() {
        return children;
    }

    /**
     * Returns if this node has children or if it is a leaf node.
     *
     * @return <code>true</code> if this node has children; <code>false</code>
     * if it does not have any children.
     */
    public boolean hasChildren() {
        if (children.length == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Gets the position of this node in the list of siblings managed by the
     * parent node. This node can be obtained by
     * <code>this = parent.children[this.index()]</code>.
     *
     * @return The index of the child array of this node's parent. If this is
     * the root node it will return -1.
     */
    public int index() {
        if (parent != null) {
            for (int i = 0;; i++) {
                Object node = parent.children[i];

                if (this == node) {
                    return i;
                }
            }
        }

        // Only ever make it here if this is the root node.
        return -1;
    }

    /**
     * Gets this node's depth in the tree. The root node will have a depth of 0,
     * first-level nodes will have a depth of 1, and so on.
     *
     * @return The depth of this node in the tree.
     */
    public int depth() {
        int depth = recurseDepth(parent, 0);
        return depth;
    }

    /**
     * Recursive helper method to calculate the depth of a node. The caller
     * should pass its parent and an initial depth of 0.
     * <p>
     * A recursive approach is used so that when a node that is part of a tree
     * is removed from that tree, we do not need to recalculate the depth of
     * every node in that subtree.
     *
     * @param node is the node to recursively check for its depth. This should
     * be set to <code>parent</code> by the caller.
     * @param depth is the depth of the current node (i.e. the child of
     * <code>node</code>). This should be set to 0 by the caller.
     */
    private int recurseDepth(TempIndexNode node, int depth) {
        if (node == null) // reached top of tree
        {
            return depth;
        } else {
            return recurseDepth(node.parent, depth + 1);
        }
    }

    /**
     * A handle to the programmer assigned object encapsulated by this node.
     * This will be <code>null</code> when the user has not assigned any data to
     * this node.
     */
    private Object m_userData;

    /**
     * Attaches a user defined object to this node. Only one object can be
     * attached to a node.
     *
     * @param userObject is the programmer defined object to attach to this node
     * in the tree. Set it to <code>null</code> to clear any objects.
     */
    public void setUserObject(Object userObject) {
        m_userData = userObject;
    }

    /**
     * Gets the user defined object attached to this node. It must be cast back
     * to what it was inserted as. It is up to the developer to make this cast.
     *
     * @return The programmer defined object attached to this node in the tree.
     * Returns <code>null</code> if no object is attached.
     */
    public Object getUserObject() {
        return m_userData;
    }

    public List search(SchedulingSandbox sb,int low, int high) {

        List res = new ArrayList();
        takestep(sb, res, low, high, null, null);

        return res;
    }

    @Override
    public void takestep(SchedulingSandbox sb,List results, int low, int high, TreeHandler walkerimpl, TreeNodeCallBack tncb) {

        int[] objs = (int[]) this.getUserObject();

        for (int i = 0; i < objs.length; i++) {
            walkerimpl.takestep(sb,results, low, high, i, tncb);
        }
        
        tncb.nodeCallBack(this);
    }

    public TempIndexNode copy(TempIndexNode newnode) {
        try {
            newnode.children = new TempIndexNode[children.length];
            for (int i = 0; i < children.length; i++) {
                newnode.children[i] = children[i];
            }

            newnode.m_userData = new int[((int[]) m_userData).length];
            for (int i = 0; i < ((int[]) m_userData).length; i++) {
                ((int[]) newnode.m_userData)[i] = ((int[]) m_userData)[i];
            }
            newnode.parent = this.parent;
        } catch (ArrayIndexOutOfBoundsException e) {

        }
        return newnode;
    }

    public void copyin(TempIndexNode newnode) {

        children = newnode.children;
        m_userData = newnode.m_userData;
        parent = newnode.parent;

    }
}
