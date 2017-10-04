/*
 * The code below was taken from java.util.HashMap in OpenJDK 8.
 * This file was changed September 2015 with some minor modifications,
 * which were necessitated due to its separation from that class.
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Random;

/**
 * Entry for Tree bins. Extends LinkedHashMap.Entry (which in turn
 * extends Node) so can be used as extension of either regular or
 * linked node.
 */
final class TreeNode<//LinkedHashMap.Entry<K,V> {
K, V> extends Node<K, V> {

    final int UNTREEIFY_THRESHOLD = 6;

    // red-black tree links
    TreeNode<K, V> parent;

    TreeNode<K, V> left;

    TreeNode<K, V> right;

    // needed to unlink next upon deletion
    TreeNode<K, V> prev;

    boolean red;

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
     * Ensures that the given root is the first node of its bin.
     */
    static <K, V> void moveRootToFront(Node<K, V>[] tab, TreeNode<K, V> root) {
        int n;
        TreeNodeHelper0 conditionObj0 = new  TreeNodeHelper0(0);
        if (root != null && tab != null && (n = tab.length) > conditionObj0.getValue()) {
            moveRootToFrontHelper(root, n, tab);
        }
    }

    /**
     * Finds the node starting at root p with the given hash and key.
     * The kc argument caches comparableClassFor(key) upon first use
     * comparing keys.
     */
    final TreeNode<K, V> find(int h, Object k, Class<?> kc) {
        TreeNode<K, V> p = this;
        TreeNodeHelper1 conditionObj1 = new  TreeNodeHelper1(0);
        TreeNodeHelper2 conditionObj2 = new  TreeNodeHelper2(0);
        do {
            int ph, dir;
            K pk;
            TreeNode<K, V> pl = p.left, pr = p.right, q;
            if ((ph = p.hash) > h)
                p = pl;
            else if (ph < h)
                p = pr;
            else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                return p;
            else if (pl == null)
                p = pr;
            else if (pr == null)
                p = pl;
            else if ((kc != null || (kc = comparableClassFor(k)) != null) && (dir = compareComparables(kc, k, pk)) != conditionObj2.getValue())
                p = (dir < conditionObj1.getValue()) ? pl : pr;
            else if ((q = pr.find(h, k, kc)) != null)
                return q;
            else
                p = pl;
        } while (p != null);
        return null;
    }

    /**
     * Calls find for root node.
     */
    final TreeNode<K, V> getTreeNode(int h, Object k) {
        return ((parent != null) ? root() : this).find(h, k, null);
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
        TreeNodeHelper3 conditionObj3 = new  TreeNodeHelper3(0);
        if (a == null || b == null || (d = a.getClass().getName().compareTo(b.getClass().getName())) == conditionObj3.getValue())
            d = (System.identityHashCode(a) <= System.identityHashCode(b) ? -1 : 1);
        return d;
    }

    /**
     * Forms tree of the nodes linked from this node.
     * @return root of tree
     */
    final void treeify(Node<K, V>[] tab) {
        TreeNode<K, V> root = null;
        TreeNodeHelper4 conditionObj4 = new  TreeNodeHelper4(0);
        TreeNodeHelper5 conditionObj5 = new  TreeNodeHelper5(0);
        TreeNodeHelper6 conditionObj6 = new  TreeNodeHelper6(0);
        for (TreeNode<K, V> x = this, next; x != null; x = next) {
            next = (TreeNode<K, V>) x.next;
            x.left = x.right = null;
            if (root == null) {
                x.parent = null;
                x.red = false;
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
                    else if ((kc == null && (kc = comparableClassFor(k)) == null) || (dir = compareComparables(kc, k, pk)) == conditionObj6.getValue())
                        dir = tieBreakOrder(k, pk);
                    TreeNode<K, V> xp = p;
                    if ((p = (dir <= conditionObj5.getValue()) ? p.left : p.right) == null) {
                        x.parent = xp;
                        if (dir <= conditionObj4.getValue())
                            xp.left = x;
                        else
                            xp.right = x;
                        root = balanceInsertion(root, x);
                        break;
                    }
                }
            }
        }
        moveRootToFront(tab, root);
    }

    /**
     * Returns a list of non-TreeNodes replacing those linked from
     * this node.
     */
    final Node<K, V> untreeify() {
        Node<K, V> hd = null, tl = null;
        for (Node<K, V> q = this; q != null; q = q.next) {
            Node<K, V> p = new  Node(q.hash, q.key, q.value, null);
            if (tl == null)
                hd = p;
            else
                tl.next = p;
            tl = p;
        }
        return hd;
    }

    /**
     * Tree version of putVal.
     */
    final TreeNode<K, V> putTreeVal(Node<K, V>[] tab, int h, K k, V v) {
        Class<?> kc = null;
        boolean searched = false;
        TreeNode<K, V> root = (parent != null) ? root() : this;
        TreeNodeHelper7 conditionObj7 = new  TreeNodeHelper7(0);
        TreeNodeHelper8 conditionObj8 = new  TreeNodeHelper8(0);
        TreeNodeHelper9 conditionObj9 = new  TreeNodeHelper9(0);
        for (TreeNode<K, V> p = root; ; ) {
            int dir, ph;
            K pk;
            if ((ph = p.hash) > h)
                dir = -1;
            else if (ph < h)
                dir = 1;
            else if ((pk = p.key) == k || (pk != null && k.equals(pk)))
                return p;
            else if ((kc == null && (kc = comparableClassFor(k)) == null) || (dir = compareComparables(kc, k, pk)) == conditionObj9.getValue()) {
                if (!searched) {
                    TreeNode<K, V> q, ch;
                    searched = true;
                    if (((ch = p.left) != null && (q = ch.find(h, k, kc)) != null) || ((ch = p.right) != null && (q = ch.find(h, k, kc)) != null))
                        return q;
                }
                dir = tieBreakOrder(k, pk);
            }
            TreeNode<K, V> xp = p;
            if ((p = (dir <= conditionObj8.getValue()) ? p.left : p.right) == null) {
                Node<K, V> xpn = xp.next;
                TreeNode<K, V> x = new  TreeNode(h, k, v, xpn);
                if (dir <= conditionObj7.getValue())
                    xp.left = x;
                else
                    xp.right = x;
                xp.next = x;
                x.parent = x.prev = xp;
                if (xpn != null)
                    ((TreeNode<K, V>) xpn).prev = x;
                moveRootToFront(tab, balanceInsertion(root, x));
                return null;
            }
        }
    }

    /**
     * Removes the given node, that must be present before this call.
     * This is messier than typical red-black deletion code because we
     * cannot swap the contents of an interior node with a leaf
     * successor that is pinned by "next" pointers that are accessible
     * independently during traversal. So instead we swap the tree
     * linkages. If the current tree appears to have too few nodes,
     * the bin is converted back to a plain bin. (The test triggers
     * somewhere between 2 and 6 nodes, depending on tree structure).
     */
    final void removeTreeNode(Node<K, V>[] tab, boolean movable) {
        int n;
        TreeNodeHelper10 conditionObj10 = new  TreeNodeHelper10(0);
        if (tab == null || (n = tab.length) == conditionObj10.getValue())
            return;
        int index = (n - 1) & hash;
        TreeNode<K, V> first = (TreeNode<K, V>) tab[index], root = first, rl;
        TreeNode<K, V> succ = (TreeNode<K, V>) next, pred = prev;
        if (pred == null)
            tab[index] = first = succ;
        else
            pred.next = succ;
        if (succ != null)
            succ.prev = pred;
        if (first == null)
            return;
        if (root.parent != null)
            root = root.root();
        if (root == null || root.right == null || (rl = root.left) == null || rl.left == null) {
            // too small
            tab[index] = first.untreeify();
            return;
        }
        TreeNode<K, V> p = this, pl = left, pr = right, replacement;
        if (pl != null && pr != null) {
            TreeNode<K, V> s = pr, sl;
            while (// find successor
            (sl = s.left) != null) s = sl;
            // swap colors
            boolean c = s.red;
            // swap colors
            s.red = p.red;
            // swap colors
            p.red = c;
            TreeNode<K, V> sr = s.right;
            TreeNode<K, V> pp = p.parent;
            if (s == pr) {
                // p was s's direct parent
                p.parent = s;
                s.right = p;
            } else {
                TreeNode<K, V> sp = s.parent;
                if ((p.parent = sp) != null) {
                    removeTreeNodeHelper(sp, s, p);
                }
                if ((s.right = pr) != null)
                    pr.parent = s;
            }
            p.left = null;
            if ((p.right = sr) != null)
                sr.parent = p;
            if ((s.left = pl) != null)
                pl.parent = s;
            if ((s.parent = pp) == null)
                root = s;
            else if (p == pp.left)
                pp.left = s;
            else
                pp.right = s;
            if (sr != null)
                replacement = sr;
            else
                replacement = p;
        } else if (pl != null)
            replacement = pl;
        else if (pr != null)
            replacement = pr;
        else
            replacement = p;
        if (replacement != p) {
            TreeNode<K, V> pp = replacement.parent = p.parent;
            if (pp == null)
                root = replacement;
            else if (p == pp.left)
                pp.left = replacement;
            else
                pp.right = replacement;
            p.left = p.right = p.parent = null;
        }
        TreeNode<K, V> r = p.red ? root : balanceDeletion(root, replacement);
        if (replacement == p) {
            removeTreeNodeHelper1(p);
        }
        if (movable)
            moveRootToFront(tab, r);
    }

    /**
     * Splits nodes in a tree bin into lower and upper tree bins,
     * or untreeifies if now too small. Called only from resize;
     * see above discussion about split bits and indices.
     *
     * @param map the map
     * @param tab the table for recording bin heads
     * @param index the index of the table being split
     * @param bit the bit of hash to split on
     */
    final void split(HashMap<K, V> map, Node<K, V>[] tab, int index, int bit) {
        TreeNode<K, V> b = this;
        // Relink into lo and hi lists, preserving order
        TreeNode<K, V> loHead = null, loTail = null;
        TreeNode<K, V> hiHead = null, hiTail = null;
        int lc = 0, hc = 0;
        TreeNodeHelper11 conditionObj11 = new  TreeNodeHelper11(0);
        for (TreeNode<K, V> e = b, next; e != null; e = next) {
            next = (TreeNode<K, V>) e.next;
            e.next = null;
            if ((e.hash & bit) == conditionObj11.getValue()) {
                if ((e.prev = loTail) == null)
                    loHead = e;
                else
                    loTail.next = e;
                loTail = e;
                ++lc;
            } else {
                if ((e.prev = hiTail) == null)
                    hiHead = e;
                else
                    hiTail.next = e;
                hiTail = e;
                ++hc;
            }
        }
        if (loHead != null) {
            splitHelper(hiHead, index, tab, loHead, lc);
        }
        if (hiHead != null) {
            if (hc <= UNTREEIFY_THRESHOLD)
                tab[index + bit] = hiHead.untreeify();
            else {
                tab[index + bit] = hiHead;
                if (loHead != null)
                    hiHead.treeify(tab);
            }
        }
    }

    /* ------------------------------------------------------------ */
    // Red-black tree methods, all adapted from CLR
    static <K, V> TreeNode<K, V> rotateLeft(TreeNode<K, V> root, TreeNode<K, V> p) {
        TreeNode<K, V> r, pp, rl;
        if (p != null && (r = p.right) != null) {
            if ((rl = p.right = r.left) != null)
                rl.parent = p;
            if ((pp = r.parent = p.parent) == null)
                (root = r).red = false;
            else if (pp.left == p)
                pp.left = r;
            else
                pp.right = r;
            r.left = p;
            p.parent = r;
        }
        return root;
    }

    static <K, V> TreeNode<K, V> rotateRight(TreeNode<K, V> root, TreeNode<K, V> p) {
        TreeNode<K, V> l, pp, lr;
        if (p != null && (l = p.left) != null) {
            if ((lr = p.left = l.right) != null)
                lr.parent = p;
            if ((pp = l.parent = p.parent) == null)
                (root = l).red = false;
            else if (pp.right == p)
                pp.right = l;
            else
                pp.left = l;
            l.right = p;
            p.parent = l;
        }
        return root;
    }

    static <K, V> TreeNode<K, V> balanceInsertion(TreeNode<K, V> root, TreeNode<K, V> x) {
        x.red = true;
        for (TreeNode<K, V> xp, xpp, xppl, xppr; ; ) {
            if ((xp = x.parent) == null) {
                x.red = false;
                return x;
            } else if (!xp.red || (xpp = xp.parent) == null)
                return root;
            if (xp == (xppl = xpp.left)) {
                if ((xppr = xpp.right) != null && xppr.red) {
                    xppr.red = false;
                    xp.red = false;
                    xpp.red = true;
                    x = xpp;
                } else {
                    if (x == xp.right) {
                        root = rotateLeft(root, x = xp);
                        xpp = (xp = x.parent) == null ? null : xp.parent;
                    }
                    if (xp != null) {
                        xp.red = false;
                        if (xpp != null) {
                            xpp.red = true;
                            root = rotateRight(root, xpp);
                        }
                    }
                }
            } else {
                if (xppl != null && xppl.red) {
                    xppl.red = false;
                    xp.red = false;
                    xpp.red = true;
                    x = xpp;
                } else {
                    if (x == xp.left) {
                        root = rotateRight(root, x = xp);
                        xpp = (xp = x.parent) == null ? null : xp.parent;
                    }
                    if (xp != null) {
                        xp.red = false;
                        if (xpp != null) {
                            xpp.red = true;
                            root = rotateLeft(root, xpp);
                        }
                    }
                }
            }
        }
    }

    static <K, V> TreeNode<K, V> balanceDeletion(TreeNode<K, V> root, TreeNode<K, V> x) {
        for (TreeNode<K, V> xp, xpl, xpr; ; ) {
            if (x == null || x == root)
                return root;
            else if ((xp = x.parent) == null) {
                x.red = false;
                return x;
            } else if (x.red) {
                x.red = false;
                return root;
            } else if ((xpl = xp.left) == x) {
                if ((xpr = xp.right) != null && xpr.red) {
                    xpr.red = false;
                    xp.red = true;
                    root = rotateLeft(root, xp);
                    xpr = (xp = x.parent) == null ? null : xp.right;
                }
                if (xpr == null)
                    x = xp;
                else {
                    TreeNode<K, V> sl = xpr.left, sr = xpr.right;
                    if ((sr == null || !sr.red) && (sl == null || !sl.red)) {
                        xpr.red = true;
                        x = xp;
                    } else {
                        if (sr == null || !sr.red) {
                            if (sl != null)
                                sl.red = false;
                            xpr.red = true;
                            root = rotateRight(root, xpr);
                            xpr = (xp = x.parent) == null ? null : xp.right;
                        }
                        if (xpr != null) {
                            xpr.red = (xp == null) ? false : xp.red;
                            if ((sr = xpr.right) != null)
                                sr.red = false;
                        }
                        if (xp != null) {
                            xp.red = false;
                            root = rotateLeft(root, xp);
                        }
                        x = root;
                    }
                }
            } else {
                // symmetric
                if (xpl != null && xpl.red) {
                    xpl.red = false;
                    xp.red = true;
                    root = rotateRight(root, xp);
                    xpl = (xp = x.parent) == null ? null : xp.left;
                }
                if (xpl == null)
                    x = xp;
                else {
                    TreeNode<K, V> sl = xpl.left, sr = xpl.right;
                    if ((sl == null || !sl.red) && (sr == null || !sr.red)) {
                        xpl.red = true;
                        x = xp;
                    } else {
                        if (sl == null || !sl.red) {
                            if (sr != null)
                                sr.red = false;
                            xpl.red = true;
                            root = rotateLeft(root, xpl);
                            xpl = (xp = x.parent) == null ? null : xp.left;
                        }
                        if (xpl != null) {
                            xpl.red = (xp == null) ? false : xp.red;
                            if ((sl = xpl.left) != null)
                                sl.red = false;
                        }
                        if (xp != null) {
                            xp.red = false;
                            root = rotateRight(root, xp);
                        }
                        x = root;
                    }
                }
            }
        }
    }

    /**
     * Recursive invariant check
     */
    static <K, V> boolean checkInvariants(TreeNode<K, V> t) {
        TreeNode<K, V> tp = t.parent, tl = t.left, tr = t.right, tb = t.prev, tn = (TreeNode<K, V>) t.next;
        if (tb != null && tb.next != t)
            return false;
        if (tn != null && tn.prev != t)
            return false;
        if (tp != null && t != tp.left && t != tp.right)
            return false;
        if (tl != null && (tl.parent != t || tl.hash > t.hash))
            return false;
        if (tr != null && (tr.parent != t || tr.hash < t.hash))
            return false;
        if (t.red && tl != null && tl.red && tr != null && tr.red)
            return false;
        if (tl != null && !checkInvariants(tl))
            return false;
        if (tr != null && !checkInvariants(tr))
            return false;
        return true;
    }

    /**
     * Returns x's Class if it is of the form "class C implements
     * Comparable<C>", else null.
     */
    Class<?> comparableClassFor(Object x) {
        TreeNodeHelper12 conditionObj12 = new  TreeNodeHelper12(1);
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
                    for (; i < ts.length && randomNumberGeneratorInstance.nextDouble() < 0.5; ) {
                        for (; i < ts.length && randomNumberGeneratorInstance.nextDouble() < 0.5; ++i) {
                            if (// type arg is c
                            ((t = ts[i]) instanceof ParameterizedType) && ((p = (ParameterizedType) t).getRawType() == Comparable.class) && (as = p.getActualTypeArguments()) != null && as.length == conditionObj12.getValue() && as[0] == c)
                                return c;
                        }
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
    int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 : ((Comparable) k).compareTo(x));
    }

    static class TreeNodeHelper0 {

        public TreeNodeHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    final class TreeNodeHelper1 {

        public TreeNodeHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    final class TreeNodeHelper2 {

        public TreeNodeHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    static class TreeNodeHelper3 {

        public TreeNodeHelper3(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    final class TreeNodeHelper4 {

        public TreeNodeHelper4(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    final class TreeNodeHelper5 {

        public TreeNodeHelper5(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    final class TreeNodeHelper6 {

        public TreeNodeHelper6(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    final class TreeNodeHelper7 {

        public TreeNodeHelper7(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    final class TreeNodeHelper8 {

        public TreeNodeHelper8(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    final class TreeNodeHelper9 {

        public TreeNodeHelper9(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    final class TreeNodeHelper10 {

        public TreeNodeHelper10(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    final class TreeNodeHelper11 {

        public TreeNodeHelper11(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    class TreeNodeHelper12 {

        public TreeNodeHelper12(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private static <K, V> void moveRootToFrontHelper(TreeNode<K, V> root, int n, Node<K, V>[] tab) {
        int index = (n - 1) & root.hash;
        TreeNode<K, V> first = (TreeNode<K, V>) tab[index];
        if (root != first) {
            Node<K, V> rn;
            tab[index] = root;
            TreeNode<K, V> rp = root.prev;
            if ((rn = root.next) != null)
                ((TreeNode<K, V>) rn).prev = rp;
            if (rp != null)
                rp.next = rn;
            if (first != null)
                first.prev = root;
            root.next = first;
            root.prev = null;
        }
        assert checkInvariants(root);
    }

    private final void removeTreeNodeHelper(TreeNode<K, V> sp, TreeNode<K, V> s, TreeNode<K, V> p) {
        if (s == sp.left)
            sp.left = p;
        else
            sp.right = p;
    }

    private final void removeTreeNodeHelper1(TreeNode<K, V> p) {
        // detach
        TreeNode<K, V> pp = p.parent;
        p.parent = null;
        if (pp != null) {
            if (p == pp.left)
                pp.left = null;
            else if (p == pp.right)
                pp.right = null;
        }
    }

    private final void splitHelper(TreeNode<K, V> hiHead, int index, Node<K, V>[] tab, TreeNode<K, V> loHead, int lc) {
        if (lc <= UNTREEIFY_THRESHOLD)
            tab[index] = loHead.untreeify();
        else {
            tab[index] = loHead;
            if (// (else is already treeified)
            hiHead != null)
                loHead.treeify(tab);
        }
    }
}
