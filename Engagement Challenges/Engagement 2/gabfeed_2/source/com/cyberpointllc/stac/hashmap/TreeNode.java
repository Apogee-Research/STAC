/**
 * The code below was taken from java.util.HashMap in OpenJDK 8.
 * This file was changed September 2015 with some minor modifications,
 * which were necessitated due to its separation from that class.
 * Additionally, tree balancing was removed.
 * 
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.cyberpointllc.stac.hashmap;

import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import java.util.Random;

public class TreeNode<K, V> extends Node<K, V> {

    // red-black tree links
    TreeNode<K, V> parent;

    TreeNode<K, V> left;

    TreeNode<K, V> right;

    // needed to unlink next upon deletion
    TreeNode<K, V> prev;

    TreeNode(int hash, K key, V val, Node<K, V> next) {
        super(hash, key, val, next);
    }

    /**
     * Returns root of tree containing this node.
     */
    final TreeNode<K, V> root() {
        for (TreeNode<K, V> r = this, p; ; ) {
            if ((p = r.parent) == null)
                return r;
            r = p;
        }
    }

    /**
     * Finds the node starting at root p with the given hash and key.
     * The kc argument caches comparableClassFor(key) upon first use
     * comparing keys.
     */
    final TreeNode<K, V> find(int h, Object k, Class<?> kc) {
        TreeNode<K, V> p = this;
        do {
            int ph, dir;
            K pk;
            TreeNode<K, V> pl = p.left, pr = p.right, q;
            if ((ph = p.hash) > h) {
                p = pl;
            } else if (ph < h) {
                p = pr;
            } else if ((pk = p.getKey()) == k || (k != null && k.equals(pk))) {
                return p;
            } else if (pl == null) {
                p = pr;
            } else if (pr == null) {
                p = pl;
            } else if ((kc != null || (kc = comparableClassFor(k)) != null) && (dir = compareComparables(kc, k, pk)) != 0) {
                p = (dir < 0) ? pl : pr;
            } else if ((q = pr.find(h, k, kc)) != null) {
                return q;
            } else {
                p = pl;
            }
        } while (p != null);
        return null;
    }

    /**
     * splice this node out of the tree.  See CLR TREE-DELETE
     */
    final void removeTreeNode(Node<K, V>[] tab) {
        //System.out.println("Treenode.remove " + this.key);
        int index = hash(key, tab.length);
        // candidate is the actual node that gets removed (if it's not this, we give its data to this node)
        TreeNode<K, V> candidate, child;
        if (left == null || right == null) {
            candidate = this;
        } else {
            candidate = this.successor();
        }
        if (candidate.left != null) {
            child = candidate.left;
        } else {
            child = candidate.right;
        }
        if (child != null) {
            child.parent = candidate.parent;
        }
        if (candidate.parent == null) {
            removeTreeNodeHelper(child, index, tab);
        } else if (candidate == candidate.parent.left) {
            candidate.parent.left = child;
        } else {
            candidate.parent.right = child;
        }
        if (!candidate.equals(this)) {
            removeTreeNodeHelper1(candidate);
        }
    }

    /**
     * See CLR TREE-SUCCESSOR
     * @return
     */
    private TreeNode<K, V> successor() {
        if (right != null) {
            return right.minimum();
        }
        TreeNode candidate = parent;
        TreeNode child = this;
        while (candidate != null && child.equals(candidate.right)) {
            child = candidate;
            candidate = candidate.parent;
        }
        return candidate;
    }

    private TreeNode<K, V> minimum() {
        TreeNode<K, V> candidate = this;
        while (candidate.left != null) {
            candidate = candidate.left;
        }
        return candidate;
    }

    /**
     * Tree version of putVal. From OpenJDK 8, HashMap.TreeNode.  Licensed under GPL2
     */
    final TreeNode<K, V> putTreeVal(Node<K, V>[] tab, int h, K k, V v) {
        Class<?> kc = null;
        boolean searched = false;
        TreeNode<K, V> root = (parent != null) ? root() : this;
        for (TreeNode<K, V> p = root; ; ) {
            int dir, ph;
            K pk;
            if ((ph = p.hash) > h)
                dir = -1;
            else if (ph < h)
                dir = 1;
            else if ((pk = p.key) == k || (pk != null && k.equals(pk)))
                return p;
            else if ((kc == null && (kc = comparableClassFor(k)) == null) || (dir = compareComparables(kc, k, pk)) == 0) {
                if (!searched) {
                    TreeNode<K, V> q, ch;
                    searched = true;
                    if (((ch = p.left) != null && (q = ch.find(h, k, kc)) != null) || ((ch = p.right) != null && (q = ch.find(h, k, kc)) != null))
                        return q;
                }
                dir = tieBreakOrder(k, pk);
            }
            TreeNode<K, V> xp = p;
            if ((p = (dir <= 0) ? p.left : p.right) == null) {
                Node<K, V> xpn = xp.next;
                TreeNode<K, V> x = new  TreeNode(h, k, v, xpn);
                if (dir <= 0)
                    xp.left = x;
                else
                    xp.right = x;
                xp.next = x;
                x.parent = x.prev = xp;
                if (xpn != null)
                    ((TreeNode<K, V>) xpn).prev = x;
                //moveRootToFront(tab, balanceInsertion(root, x));
                return null;
            }
        }
    }

    /**
     * Calls find for root node.
     */
    final TreeNode<K, V> getTreeNode(int h, Object k) {
        return ((parent != null) ? root() : this).find(h, k, null);
    }

    /**
     * Returns x's Class if it is of the form "class C implements
     * Comparable<C>", else null.
     */
    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c;
            Type[] ts, as;
            Type t;
            ParameterizedType p;
            if (// bypass checks
            (c = x.getClass()) == String.class)
                return c;
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ) {
                    Random randomNumberGeneratorInstance = new  Random();
                    for (; i < ts.length && randomNumberGeneratorInstance.nextDouble() < 0.5; ++i) {
                        if (// type arg is c
                        ((t = ts[i]) instanceof ParameterizedType) && ((p = (ParameterizedType) t).getRawType() == Comparable.class) && (as = p.getActualTypeArguments()) != null && as.length == 1 && as[0] == c)
                            return c;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns k.compareTo(x) if x matches kc (k's screened comparable
     * class), else 0.
     */
    // for cast to Comparable
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 : ((Comparable) k).compareTo(x));
    }

    /**
     * Tie-breaking utility for ordering insertions when equal
     * hashCodes and non-comparable. We don't require a total
     * order, just a consistent insertion rule to maintain
     * equivalence across rebalancings. Tie-breaking further than
     * necessary simplifies testing a bit.
     */
    static int tieBreakOrder(Object a, Object b) {
        int d;
        if (a == null || b == null || (d = a.getClass().getName().compareTo(b.getClass().getName())) == 0)
            d = (System.identityHashCode(a) <= System.identityHashCode(b) ? -1 : 1);
        return d;
    }

    public void treeify(Node<K, V>[] tab) {
        TreeNode<K, V> root = null;
        for (TreeNode<K, V> x = this, next; x != null; x = next) {
            next = (TreeNode<K, V>) x.next;
            x.left = x.right = null;
            if (root == null) {
                x.parent = null;
                root = x;
            } else {
                K k = x.key;
                int h = x.hash;
                Class<?> kc = null;
                for (TreeNode<K, V> p = root; ; ) {
                    int dir, ph;
                    K pk = p.key;
                    if ((ph = p.hash) > h)
                        dir = -1;
                    else if (ph < h)
                        dir = 1;
                    else if ((kc == null && (kc = comparableClassFor(k)) == null) || (dir = compareComparables(kc, k, pk)) == 0)
                        dir = tieBreakOrder(k, pk);
                    TreeNode<K, V> xp = p;
                    if ((p = (dir <= 0) ? p.left : p.right) == null) {
                        x.parent = xp;
                        if (dir <= 0)
                            xp.left = x;
                        else
                            xp.right = x;
                        break;
                    }
                }
            }
        }
    }

    private final void removeTreeNodeHelper(TreeNode<K, V> child, int index, Node<K, V>[] tab) {
        tab[index] = child;
    }

    private final void removeTreeNodeHelper1(TreeNode<K, V> candidate) {
        this.value = candidate.value;
        this.key = candidate.key;
        this.hash = candidate.hash;
        this.next = candidate.next;
    }
}
