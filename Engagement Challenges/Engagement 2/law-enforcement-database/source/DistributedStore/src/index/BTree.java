/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package index;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
//import java.util.concurrent.CopyOnWriteArraySet;

/* 
 * Unlike a binary search tree, each node of a B-tree may have a variable number of keys and children.
 * The keys are stored in non-decreasing order. Each node either is a leaf node or
 * it has some associated children that are the root nodes of subtrees.
 * The left child node of a node's element contains all nodes (elements) with keys less than or equal to the node element's key
 * but greater than the preceding node element's key.
 * If a node becomes full, a split operation is performed during the insert operation.
 * The split operation transforms a full node with 2*T-1 elements into two nodes with T-1 elements each
 * and moves the median key of the two nodes into its parent node.
 * The elements left of the median (middle) element of the splitted node remain in the original node.
 * The new node becomes the child node immediately to the right of the median element that was moved to the parent node.
 * 
 * Example (T = 4):
 * 1.  R = | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
 * 
 * 2.  Add key 8
 *   
 * 3.  R =         | 4 |
 *                 /   \
 *     | 1 | 2 | 3 |   | 5 | 6 | 7 | 8 |
 *
 */
public class BTree {

    private static int T = 10;
    private Node mRootNode;
    private static final int LEFT_CHILD_NODE = 0;
    private static final int RIGHT_CHILD_NODE = 1;
    public boolean optimizedinserts = false;

    TransactionStack trans;
    private Object clist;

    public BTree(int t) {
        T = t;
        mRootNode = new Node();
        mRootNode.mIsLeafNode = true;
        trans = new TransactionStack(this);
    }

    public void beginTransaction() {
        trans.begin();
    }

    public void commit() {
        trans.commit();
    }

    public void rollback() {
        trans.rollback();
    }

    class IntHolderV {

        Integer[] objects = null;

        Vector<Integer> objs;

        public IntHolderV(int size) {
            objs = new Vector(1);
            //objects = new Object[size];
        }

        public int get(int index) {

            try {
                return objs.get(index);
            } catch (IndexOutOfBoundsException e) {
                return new Integer(0);
            }
            //return objects[index];
        }

        public void put(int index, int i) {
            Integer integer = new Integer(i);
            //objects[index] = o;
            if (objs.size() < index) {
                //  objs.add(null);
                //objs.ensureCapacity(index);
                //put(index,o);
            }

            try {
                objs.add(index, integer);
            } catch (IndexOutOfBoundsException e) {
                objs.add(null);
                objs.ensureCapacity(index);
                put(index, integer);

            }
        }

    }

    public static void printNode(Node n) {

        System.out.println("isleaf:" + n.mIsLeafNode);
        for (int i = 0; i < n.mKeys.length; i++) {
            System.out.print(n.mKeys[i] + " : ");
        }
        System.out.println("");

    }

    public class Node {

        public int xtrasize = 0;

        public int mNumKeys = 0;
        public int[] mKeys;
        public Object[] mObjects;
        public Node[] mChildNodes;
        public boolean mIsLeafNode;
        public boolean[] isfastDeleted;
        public int[] permissions;

        //public  CopyOnWriteArrayList<Integer> fastSearch;
        public Vector<Integer> fastSearch;
        //public  Integer[] fastSearch;

        Node parent;
        public ConcurrentHashMap<Integer, Object> instantSearch;
        private Integer[] fastSearchArray;
        private boolean isremotenode = false;

        public Node() {
            this(0);
        }

        public Node(int size) {
            xtrasize = size;
            mKeys = new int[2 * T - 1 + xtrasize];
            mObjects = new Object[2 * T - 1 + xtrasize];
            mChildNodes = new Node[2 * T + xtrasize];
            isfastDeleted = new boolean[2 * T - 1 + xtrasize];
            permissions = new int[2 * T - 1 + xtrasize];
        }

        public void increaseCapacity(int s) {

            Node copy = copy(s);
            copyin(copy);

        }

        public Node copy(int s) {

            xtrasize += s;
            return copy(null);
        }

        public Node copy(Node newnode) {

            if (newnode == null) {
                newnode = new Node(this.xtrasize);
            }
            try {

                newnode.mNumKeys = mNumKeys;
                for (int i = 0; i < mKeys.length; i++) {
                    newnode.mKeys[i] = mKeys[i];
                }
                for (int i = 0; i < mObjects.length; i++) {
                    newnode.mObjects[i] = mObjects[i];
                }
                for (int i = 0; i < mChildNodes.length; i++) {
                    newnode.mChildNodes[i] = mChildNodes[i];
                }
                for (int i = 0; i < isfastDeleted.length; i++) {
                    newnode.isfastDeleted[i] = isfastDeleted[i];
                }
                for (int i = 0; i < permissions.length; i++) {
                    newnode.permissions[i] = permissions[i];
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                return copy(new Node(this.xtrasize + 1));
            }

            newnode.mIsLeafNode = mIsLeafNode;
            return newnode;

        }

        public void copyin(Node innode) {

            this.mNumKeys = innode.mNumKeys;
            /*for(int i=0;i<innode.mKeys.length;i++){
             this.mKeys[i] = innode.mKeys[i];
             }
             for(int i=0;i<innode.mObjects.length;i++){
             this.mObjects[i] = innode.mObjects[i];
             }
             for(int i=0;i<innode.mChildNodes.length;i++){
             this.mChildNodes[i] = innode.mChildNodes[i];
             }
             for(int i=0;i<innode.isfastDeleted.length;i++){
             this.isfastDeleted[i] = innode.isfastDeleted[i];
             }
             for(int i=0;i<innode.permissions.length;i++){
             this.permissions[i] = innode.permissions[i];
             }*/
            this.mKeys = innode.mKeys;
            this.mObjects = innode.mObjects;

            this.mChildNodes = innode.mChildNodes;

            this.isfastDeleted = innode.isfastDeleted;

            this.permissions = innode.permissions;

            this.mIsLeafNode = innode.mIsLeafNode;
            this.isfastDeleted = innode.isfastDeleted;

        }

        int binarySearch(int key) {
            int leftIndex = 0;
            int rightIndex = mNumKeys - 1;

            while (leftIndex <= rightIndex) {
                final int middleIndex = leftIndex + ((rightIndex - leftIndex) / 2);
                if (mKeys[middleIndex] < key) {
                    leftIndex = middleIndex + 1;
                } else if (mKeys[middleIndex] > key) {
                    rightIndex = middleIndex - 1;
                } else {
                    return middleIndex;
                }
            }

            return -1;
        }

        boolean contains(int key) {
            return binarySearch(key) != -1;
        }

        // Remove an element from a node and also the left (0) or right (+1) child.
        void remove(int index, int leftOrRightChild) {
            if (index >= 0) {
                int i;
                for (i = index; i < mNumKeys - 1; i++) {
                    mKeys[i] = mKeys[i + 1];
                    mObjects[i] = mObjects[i + 1];
                    if (!mIsLeafNode) {
                        if (i >= index + leftOrRightChild) {
                            mChildNodes[i] = mChildNodes[i + 1];
                        }
                    }
                }
                mKeys[i] = 0;
                mObjects[i] = null;
                if (!mIsLeafNode) {
                    if (i >= index + leftOrRightChild) {
                        mChildNodes[i] = mChildNodes[i + 1];
                    }
                    mChildNodes[i + 1] = null;
                }
                mNumKeys--;
            }
        }

        void shiftRightByOne() {
            if (!mIsLeafNode) {
                mChildNodes[mNumKeys + 1] = mChildNodes[mNumKeys];
            }
            for (int i = mNumKeys - 1; i >= 0; i--) {
                mKeys[i + 1] = mKeys[i];
                mObjects[i + 1] = mObjects[i];
                if (!mIsLeafNode) {
                    mChildNodes[i + 1] = mChildNodes[i];
                }
            }
        }

        int subtreeRootNodeIndex(int key) {
            for (int i = 0; i < mNumKeys; i++) {
                if (key < mKeys[i]) {
                    return i;
                }
            }
            return mNumKeys;
        }
    }

    public BTree() {
        mRootNode = new Node();
        mRootNode.mIsLeafNode = true;
    }

    //static List<Long> splits = new ArrayList<Long>();
    //static List<Long> nonsplits = new ArrayList<Long>();
    public boolean add(int key, Object object, boolean log) {
        //int split = 0;
        //spitcnode = false;
        //long start = 0;

        /*if (log) {
         start = System.nanoTime();
         }*/
        Node rootNode = mRootNode;
        if (!update(mRootNode, key, object)) {
            if (rootNode.mNumKeys == (2 * T - 1)) {
                Node newRootNode = new Node();
                mRootNode = newRootNode;
                newRootNode.mIsLeafNode = false;
                mRootNode.mChildNodes[0] = rootNode;
                splitChildNode(newRootNode, 0, rootNode); // Split rootNode and move its median (middle) key up into newRootNode.
                //split = 1;
                //splits1
                insertIntoNonFullNode(newRootNode, key, object, null); // Insert the key into the B-Tree with root newRootNode.
            } else {
                //nonsplits++;
                //split = 2;
                insertIntoNonFullNode(rootNode, key, object, null); // Insert the key into the B-Tree with root rootNode.
            }
        }
        /*if (log) {
         long time = System.nanoTime() - start;
         //System.out.println("split"+split);

         //System.out.println("time"+time);
         if (log) {
         if (spitcnode) {
         splits.add(time);
         } else //if (spitcnode)
         {
         nonsplits.add(time);
         }
         }
         }*/
        //System.out.println("time to add:"+time);
        return true;//
        //return spitcnode;//+":"+time;
        //return true;
        //System.out.printf(" each add/remove took an average of %.1f ns%n",   (double) time/runs);

    }

    /*public static void calculateAverages() {
     System.out.println("splits:" + calculateAverage(splits) + " size:" + splits.size() + "\n");
     System.out.println("nonsplits:" + calculateAverage(nonsplits) + " size:" + nonsplits.size());
     }

     private static double calculateAverage(List<Long> marks) {
     Long lowestval = 100000000L;
     Long highestval = 0L;
     Long sum = (long) 0;
     if (!marks.isEmpty()) {
     for (Long mark : marks) {
     if (mark < lowestval) {
     lowestval = mark;
     }
     if (mark > highestval) {
     highestval = mark;
     }
     sum += mark;
     }
     System.out.println("low range:" + lowestval);
     System.out.println("high range:" + highestval);
     System.out.println("size:" + marks.size());
     Double r = sum.doubleValue() / marks.size();
     marks.clear();
     return r;
     }
     System.out.println("low range:" + lowestval);

     System.out.println("high range:" + highestval);
     marks.clear();
     return sum;
     }*/
    /*
     public void calcsplits(){
     long savg = splittimes/splits;
     long nsavg = nonsplittimes/nonsplits;
            
     System.out.println("savg"+savg + ":"+splits);
     System.out.println("nsavg"+nsavg+":"+nonsplits);
     }*/
    void splitPotentialRemoteNode(Node parentNode, int newsize, int itertions) {

        if (parentNode.mNumKeys == ((itertions * newsize))) {

            Node newTempPNode = new Node(itertions);
            newTempPNode.fastSearch = new Vector<Integer>();

            newTempPNode.mNumKeys = itertions;

            for (int x = 0; x < itertions * newsize; x += newsize) {

                Node newNode = new Node(newsize - (T*2-1));
                newTempPNode.mChildNodes[x / newsize] = newNode;
                for (int y = 0; y < newsize; y++) {
                    newNode.mKeys[y] = parentNode.mKeys[x + y];
                    if (y == (newsize - 1)) {//) && x< (T-1)){

                        newTempPNode.fastSearch.ensureCapacity(x / newsize);
                        newTempPNode.fastSearch.add(x / newsize, parentNode.mKeys[x + y]);
                        newTempPNode.mKeys[x / newsize] = parentNode.mKeys[x + y];

                    }
                    newNode.mObjects[y] = parentNode.mObjects[x + y];

                    newNode.mNumKeys = y + 1;
                    newNode.mChildNodes[y] = parentNode.mChildNodes[x + y];
                    if ((x + y) == (itertions * newsize) - 1) {
                        if (parentNode.mChildNodes[x + y + 1] != null) {
                            newNode.mChildNodes[y + 1] = parentNode.mChildNodes[x + y + 1];
                        }
                    }
                }
            }

            parentNode.mNumKeys = newTempPNode.mNumKeys;
            parentNode.fastSearch = new Vector<Integer>();
            for (int i = 0; i < parentNode.mChildNodes.length; i++) {
                parentNode.mChildNodes[i] = null;
            }
            for (int i = 0; i < parentNode.mKeys.length; i++) {
                parentNode.mKeys[i] = 0;
            }
            parentNode.mObjects = null;
            parentNode.isremotenode = true;

            for (int x = 0; x < newTempPNode.mNumKeys; x++) {
                parentNode.mChildNodes[x] = newTempPNode.mChildNodes[x];
                parentNode.fastSearch.ensureCapacity(x + 1);
                parentNode.fastSearch.add(x, newTempPNode.fastSearch.get(x));
                parentNode.mKeys[x] = newTempPNode.mKeys[x];
            }
            //System.out.println("see how that went");

        }
    }

    //STAC: This should be commented out, if it's not call me out on that
    //static public boolean spitcnode = false;
    // Split the node, node, of a B-Tree into two nodes that both contain T-1 elements and move node's median key up to the parentNode.
    // This method will only be called if node is full; node is the i-th child of parentNode.
    void splitChildNode(Node parentNode, int i, Node node) {

        //STAC: This next line should be commented out, if it's not call me out on that
        //spitcnode = true;
        //THIS IS JUST ALL THE SPLITING
        Node newNode = new Node();
        newNode.mIsLeafNode = node.mIsLeafNode;
        newNode.mNumKeys = T - 1;
        for (int j = 0; j < T - 1; j++) { // Copy the last T-1 elements of node into newNode.
            newNode.mKeys[j] = node.mKeys[j + T];
            newNode.mObjects[j] = node.mObjects[j + T];
        }

        if (!newNode.mIsLeafNode) {
            for (int j = 0; j < T; j++) { // Copy the last T pointers of node into newNode.
                newNode.mChildNodes[j] = node.mChildNodes[j + T];
            }
            for (int j = T; j <= node.mNumKeys; j++) {
                node.mChildNodes[j] = null;
            }
        }
        for (int j = T; j < node.mNumKeys; j++) {
            node.mKeys[j] = 0;
            node.mObjects[j] = null;
        }
        node.mNumKeys = T - 1;

        //STAC: SO THIS EXCEPTION CATHING IS UGLY, BUT IT SLOWS THINGS DOWN
        try {
            setupMedian(newNode, parentNode, i, node);
        } catch (java.lang.ArrayIndexOutOfBoundsException aex) {
            parentNode.increaseCapacity(parentNode.xtrasize + 1);
            setupMedian(newNode, parentNode, i, node);

        }

    }

    void setupMedian(Node newNode, Node parentNode, int i, Node node) {

        //STAC: Sleep call should be commented out
        /*try {
         if(this.optimizedinserts==true)
         Thread.sleep(15);
         } catch (InterruptedException ex) {
         Logger.getLogger(BTree.class.getName()).log(Level.SEVERE, null, ex);
         }*/
        // Insert a (child) pointer to node newNode into the parentNode, moving other keys and pointers as necessary.
        for (int j = parentNode.mNumKeys; j >= i + 1; j--) {
            parentNode.mChildNodes[j + 1] = parentNode.mChildNodes[j];
        }
        parentNode.mChildNodes[i + 1] = newNode;
        for (int j = parentNode.mNumKeys - 1; j >= i; j--) {
            parentNode.mKeys[j + 1] = parentNode.mKeys[j];
            parentNode.mObjects[j + 1] = parentNode.mObjects[j];
        }
        parentNode.mKeys[i] = node.mKeys[T - 1];

        parentNode.mObjects[i] = node.mObjects[T - 1];
        node.mKeys[T - 1] = 0;
        node.mObjects[T - 1] = null;
        parentNode.mNumKeys++;

        //STAC: Populate the optimization tables, this makes things slow at split time-- but it all gets used and might
        //make searches faster (lol)
        if (this.optimizedinserts == true) {
            //int lastval = 0;
            //CopyOnWriteArrayList<Integer> clist=null;
            //Vector<Integer> clist=null;
            if (this.optimizedinserts == true) {
                //STAC: Quite a few data structures tried here, some still remain in legacy comments (just in case)
                // parentNode.fastSearch = new  CopyOnWriteArrayList(new ConcurrentSkipListSet<Integer>());
                //parentNode.fastSearch = new CopyOnWriteArrayList(new Vector<Integer>());
                parentNode.fastSearch = new Vector<Integer>(1, 1);
                parentNode.instantSearch = new ConcurrentHashMap< Integer, Object>(1);//Vector<Integer>(1,1);
                //parentNode.fastSearch = new CopyOnWriteArrayList( parentNode.instantSearch.values());

            }
            for (int k = 0; k < parentNode.mKeys.length; k++) {
                if (parentNode.mKeys[k] > 0) {
                    if (this.optimizedinserts == true) {

                        parentNode.fastSearch.ensureCapacity(k + 1);
                        parentNode.fastSearch.add(parentNode.mKeys[k]);
                        String valstr = null;
                        //
                        if (parentNode.mObjects[k] != null) {
                            valstr = parentNode.mObjects[k].toString();
                            int indexOf = valstr.indexOf(":");
                            if (indexOf > 0) {
                                valstr = valstr.substring(indexOf, valstr.length());
                            } else {
                                valstr = "null";
                            }
                        } else {
                            valstr = "null";
                        }
                        //STAC: Mucking with strings make things slower
                        parentNode.instantSearch.put(parentNode.mKeys[k], Integer.toString(k) + ":" + valstr);

                    }

                }
            }

            if (parentNode.fastSearch != null) {
                //STAC:Wouldn't want this stuff being out of order -- we do a binary search later -- so lets be sure
                Collections.sort(parentNode.fastSearch);
                Integer[] toArray = parentNode.fastSearch.toArray(new Integer[parentNode.fastSearch.size()]);
                //Arrays.sort(toArray);
                parentNode.fastSearchArray = parentNode.fastSearch.toArray(toArray);
            }

        }
    }

    // Insert an element into a B-Tree. (The element will ultimately result in in an insertion into a leaf node). 
    void insertIntoNonFullNode(Node node, int key, Object object, Node parent) {
        int i = node.mNumKeys - 1;

        //STAC: This case is basic and as fast as possible, it happens on every insert
        if (node.mIsLeafNode) {
            trans.addInsert(null, null, key);
            // Since node is not a full node insert the new element into its proper place within node.
            while (i >= 0 && key < node.mKeys[i]) {
                node.mKeys[i + 1] = node.mKeys[i];
                node.mObjects[i + 1] = node.mObjects[i];
                i--;
            }
            i++;
            node.mKeys[i] = key;
            if (object != null) {
                node.mObjects[i] = object;
            }
            node.mNumKeys++;
                //STAC: (DEFERRED SPLIT) This logic tries to make it look like complicated things happen in the leaf case,
            //however, this only happens during a split because only after a split can the number of keys in the paent increment to
            //equal mNumKeys == (T*T) -- So they will have to recognize this invariant to not get confused
            if (parent != null) {

                if (this.optimizedinserts == true && parent.mIsLeafNode == false) {
                    //if (parent.mNumKeys == (100 * 2)) {
                    int newsize = 200;
                    int itertions = 2;
                    if (parent.mNumKeys == (newsize * itertions)) {
                        //System.out.println("splitPotentialRemoteNode key: "+ key);
                        splitPotentialRemoteNode(parent, newsize, itertions);
                    }
                }
                    //STAC:Line above just causes a delayed split, it allows a non-leaf node to grow bigger than typical size
                //it is an optimization to allow fast inserts, results in a less than balanced b-tree, but it allows a 
                //portion of the tree to grow predictable possibly allowing it to used remotely (hence:splitPotentialRemoteNode)
            }

        } else {
            // Move back from the last key of node until we find the child pointer to the node
            // that is the root node of the subtree where the new element should be placed.

            //STAC: The fast search is inserted after a split, it allows for fast binary searches over 
            //a longer list of keys, which happens in non-leaf nodes in optimized insert mode
            if (node.fastSearch == null) {
                while (i >= 0 && key < node.mKeys[i]) {
                    i--;
                }
                i++;

            } else if (node.fastSearch != null) {
                //STAC: don't need to do this if we don't have many keys
                if (node.mNumKeys < 20) {
                    while (i >= 0 && key < node.mKeys[i]) {
                        i--;
                    }
                    i++;
                } else {
                    //STAC: make this faster, do a binary search, keeps the time down for non-split inserts
                    int retVal = 0;
                    retVal = Arrays.binarySearch(node.fastSearchArray, key);
                    retVal = (retVal * -1) - 1;

                    i = retVal;
                }
            }

            //STAC: If this is a potentially remote node, then the data is not located in this node, so skip to the next node 
            //What do I mean by a remote node: it's a node that allows the tree to grow and split while 
            //keeping all the values in the leaves or in an independent b-tree -follows the MySQL optimizations in the Exploit paper
            //It is called remote becuase it is possible to have this placeholder node point to another b-tree located someplace else
            //this capability is convenient for keeping the b-tree medians from cascading forever upward as we add more values
            if (node.isremotenode && node.mChildNodes[i] == null) {
                //This is a remote placeholder node, it allows for decoupling of node index and node with actual data, so keep going
                //The code. right now, enables future support fot this feature, some justification for not keeping a perfectly balanced tree.
                insertIntoNonFullNode(node.mChildNodes[i - 1], key, object, node);
                return;
            }
            //ISTAC:!!!!! if this is true, we need to do a split - the side channel is in here
            if (node.mChildNodes[i].mNumKeys == (2 * T - 1)) {

                //System.out.println("key: "+ key);
                //So we enabled optimized inserts, our splitting becomes deferred until the situation above (text search for :DEFERRED SPLIT)
                //THIS ALLOWS A NON-LEAF TO GROW BIGGER, it not only becomes slower, but allows ugly cascaded splits not to screw with 
                //our test suite -- The example in the paper had similar 'optimized' functionality added to MySQL
                if (optimizedinserts) {
                    //This logic below pre-sorts everything
                    //IF WE DIDN'T DO THIS, THE SIDE CHANNEL WOULD NOT EXIST BECAUSE THE DATA ON EITHER END OF THE NEW MEDIAN
                    //WOULD BE THE SAME SIZE WHETHER THE INSERTED VALUE WAS BIGGER OR SMALLER THAN THE SECRET
                    if (node.mChildNodes[i].mIsLeafNode) {

                        Node cNode = node.mChildNodes[i];
                        trans.addInsert(cNode, node, key);
                        Node tempNode = new Node(cNode.xtrasize + 10);
                        for (int x = 0; x < cNode.mNumKeys; x++) {
                            tempNode.mChildNodes[x] = cNode.mChildNodes[x];
                            tempNode.mKeys[x] = cNode.mKeys[x];
                            tempNode.mObjects[x] = cNode.mObjects[x];
                            tempNode.mNumKeys++;
                        }
                        tempNode.mKeys[cNode.mNumKeys] = key;
                        if (object != null) {
                            tempNode.mObjects[cNode.mNumKeys] = object;
                        }
                        tempNode.mNumKeys++;

                        //for (int x = 0; x < tempNode.mNumKeys; x++) {
                        int a, b;
                        int temp;
                        Object tempobj;
                        int sortTheNumbers = tempNode.mNumKeys - 1;

                        //STAC: SORT EVERYTHING -- including the new valu
                        for (a = 0; a < sortTheNumbers; ++a) {
                            for (b = 0; b < sortTheNumbers; ++b) {
                                if (tempNode.mKeys[b] > tempNode.mKeys[b + 1]) {
                                    temp = tempNode.mKeys[b];
                                    tempobj = tempNode.mObjects[b];
                                    tempNode.mKeys[b] = tempNode.mKeys[b + 1];
                                    tempNode.mKeys[b + 1] = temp;
                                    tempNode.mObjects[b] = tempNode.mObjects[b + 1];
                                    tempNode.mObjects[b + 1] = tempobj;
                                }
                            }
                        }
                        for (int x = 0; x < tempNode.mNumKeys - 1; x++) {
                            cNode.mKeys[x] = tempNode.mKeys[x];
                            cNode.mObjects[x] = tempNode.mObjects[x];
                        }
                        key = tempNode.mKeys[tempNode.mNumKeys - 1];
                        //STAC!!!!!: SO we don't store the last value in the node to be split, 
                        //We always want to insert the greatest value last
                        //This way the left side of the split always has t+1 items, and the right side T items
                        //This difference in the newly created left and right leaves gives us our side channel

                        //System.out.println("new key: "+ key);
                        
                        object = tempNode.mObjects[tempNode.mNumKeys - 1];
                        trans.addInsert(cNode, node, key);

                        //SO NOW WE SPLIT
                        splitChildNode(node, i, node.mChildNodes[i]);
                        if (key > node.mKeys[i]) {
                            i++;
                        }
                    }
                } else {
                    //NOT AS MUCH TO DO WHEN WE DONT HAVE OPTIMIZED INSERT
                    trans.addInsert(node.mChildNodes[i], node, key);

                    splitChildNode(node, i, node.mChildNodes[i]);
                    if (key > node.mKeys[i]) {
                        i++;
                    }
                }
            }
            //Insert the value
            insertIntoNonFullNode(node.mChildNodes[i], key, object, node);
        }
    }

    public boolean delete(int key) {
        if (!optimizedinserts) {
            //long start = System.nanoTime();
            delete(mRootNode, key);
            //return System.nanoTime() - start;
        } else {
            Node n = searchForNode(key);
            fastDelete(n, key);
        }
        return true;
    }

    public void delete(Node node, int key) {

        if (node.mIsLeafNode) { // 1. If the key is in node and node is a leaf node, then delete the key from node.
            int i;
            if ((i = node.binarySearch(key)) != -1) { // key is i-th key of node if node contains key.
                node.remove(i, LEFT_CHILD_NODE);
            }
        } else {
            int i;
            if ((i = node.binarySearch(key)) != -1) { // 2. If node is an internal node and it contains the key... (key is i-th key of node if node contains key)                   
                Node leftChildNode = node.mChildNodes[i];
                Node rightChildNode = node.mChildNodes[i + 1];
                if (leftChildNode.mNumKeys >= T) { // 2a. If the predecessor child node has at least T keys...
                    Node predecessorNode = leftChildNode;
                    Node erasureNode = predecessorNode; // Make sure not to delete a key from a node with only T - 1 elements.
                    while (!predecessorNode.mIsLeafNode) { // Therefore only descend to the previous node (erasureNode) of the predecessor node and delete the key using 3.
                        erasureNode = predecessorNode;
                        predecessorNode = predecessorNode.mChildNodes[node.mNumKeys - 1];
                    }
                    node.mKeys[i] = predecessorNode.mKeys[predecessorNode.mNumKeys - 1];
                    node.mObjects[i] = predecessorNode.mObjects[predecessorNode.mNumKeys - 1];
                    delete(erasureNode, node.mKeys[i]);
                } else if (rightChildNode.mNumKeys >= T) { // 2b. If the successor child node has at least T keys...
                    Node successorNode = rightChildNode;
                    Node erasureNode = successorNode; // Make sure not to delete a key from a node with only T - 1 elements.
                    while (!successorNode.mIsLeafNode) { // Therefore only descend to the previous node (erasureNode) of the predecessor node and delete the key using 3.
                        erasureNode = successorNode;
                        successorNode = successorNode.mChildNodes[0];
                    }
                    node.mKeys[i] = successorNode.mKeys[0];
                    node.mObjects[i] = successorNode.mObjects[0];
                    delete(erasureNode, node.mKeys[i]);
                } else { // 2c. If both the predecessor and the successor child node have only T - 1 keys...
                    // If both of the two child nodes to the left and right of the deleted element have the minimum number of elements,
                    // namely T - 1, they can then be joined into a single node with 2 * T - 2 elements.
                    int medianKeyIndex = mergeNodes(leftChildNode, rightChildNode);
                    moveKey(node, i, RIGHT_CHILD_NODE, leftChildNode, medianKeyIndex); // Delete i's right child pointer from node.
                    delete(leftChildNode, key);
                }
            } else { // 3. If the key is not resent in node, descent to the root of the appropriate subtree that must contain key...
                // The method is structured to guarantee that whenever delete is called recursively on node "node", the number of keys
                // in node is at least the minimum degree T. Note that this condition requires one more key than the minimum required
                // by usual B-tree conditions. This strengthened condition allows us to delete a key from the tree in one downward pass
                // without having to "back up".
                i = node.subtreeRootNodeIndex(key);
                Node childNode = node.mChildNodes[i]; // childNode is i-th child of node.                               
                if (childNode.mNumKeys == T - 1) {
                    Node leftChildSibling = (i - 1 >= 0) ? node.mChildNodes[i - 1] : null;
                    Node rightChildSibling = (i + 1 <= node.mNumKeys) ? node.mChildNodes[i + 1] : null;
                    if (leftChildSibling != null && leftChildSibling.mNumKeys >= T) { // 3a. The left sibling has >= T keys...                                              
                        // Move a key from the subtree's root node down into childNode along with the appropriate child pointer.
                        // Therefore, first shift all elements and children of childNode right by 1.
                        childNode.shiftRightByOne();
                        childNode.mKeys[0] = node.mKeys[i - 1]; // i - 1 is the key index in node that is smaller than childNode's smallest key.
                        childNode.mObjects[0] = node.mObjects[i - 1];
                        if (!childNode.mIsLeafNode) {
                            childNode.mChildNodes[0] = leftChildSibling.mChildNodes[leftChildSibling.mNumKeys];
                        }
                        childNode.mNumKeys++;

                        // Move a key from the left sibling into the subtree's root node. 
                        node.mKeys[i - 1] = leftChildSibling.mKeys[leftChildSibling.mNumKeys - 1];
                        node.mObjects[i - 1] = leftChildSibling.mObjects[leftChildSibling.mNumKeys - 1];

                        // Remove the key from the left sibling along with its right child node.
                        leftChildSibling.remove(leftChildSibling.mNumKeys - 1, RIGHT_CHILD_NODE);
                    } else if (rightChildSibling != null && rightChildSibling.mNumKeys >= T) { // 3a. The right sibling has >= T keys...                                    
                        // Move a key from the subtree's root node down into childNode along with the appropriate child pointer.
                        childNode.mKeys[childNode.mNumKeys] = node.mKeys[i]; // i is the key index in node that is bigger than childNode's biggest key.
                        childNode.mObjects[childNode.mNumKeys] = node.mObjects[i];
                        if (!childNode.mIsLeafNode) {
                            childNode.mChildNodes[childNode.mNumKeys + 1] = rightChildSibling.mChildNodes[0];
                        }
                        childNode.mNumKeys++;

                        // Move a key from the right sibling into the subtree's root node. 
                        node.mKeys[i] = rightChildSibling.mKeys[0];
                        node.mObjects[i] = rightChildSibling.mObjects[0];

                        // Remove the key from the right sibling along with its left child node.                                                
                        rightChildSibling.remove(0, LEFT_CHILD_NODE);
                    } else { // 3b. Both of childNode's siblings have only T - 1 keys each...
                        if (leftChildSibling != null) {
                            int medianKeyIndex = mergeNodes(childNode, leftChildSibling);
                            moveKey(node, i - 1, LEFT_CHILD_NODE, childNode, medianKeyIndex); // i - 1 is the median key index in node when merging with the left sibling.                          
                        } else if (rightChildSibling != null) {
                            int medianKeyIndex = mergeNodes(childNode, rightChildSibling);
                            moveKey(node, i, RIGHT_CHILD_NODE, childNode, medianKeyIndex); // i is the median key index in node when merging with the right sibling.
                        }
                    }
                }
                delete(childNode, key);
            }
        }
    }

    public boolean calledmerge = false;

    // Merge two nodes and keep the median key (element) empty.
    int mergeNodes(Node dstNode, Node srcNode) {
        //System.out.println("mergeNodes");
        calledmerge = true;
        int medianKeyIndex;
        if (srcNode.mKeys[0] < dstNode.mKeys[dstNode.mNumKeys - 1]) {
            int i;
            // Shift all elements of dstNode right by srcNode.mNumKeys + 1 to make place for the srcNode and the median key.
            if (!dstNode.mIsLeafNode) {
                dstNode.mChildNodes[srcNode.mNumKeys + dstNode.mNumKeys + 1] = dstNode.mChildNodes[dstNode.mNumKeys];
            }
            for (i = dstNode.mNumKeys; i > 0; i--) {
                dstNode.mKeys[srcNode.mNumKeys + i] = dstNode.mKeys[i - 1];
                dstNode.mObjects[srcNode.mNumKeys + i] = dstNode.mObjects[i - 1];
                if (!dstNode.mIsLeafNode) {
                    dstNode.mChildNodes[srcNode.mNumKeys + i] = dstNode.mChildNodes[i - 1];
                }
            }

            // Clear the median key (element).
            medianKeyIndex = srcNode.mNumKeys;
            dstNode.mKeys[medianKeyIndex] = 0;
            dstNode.mObjects[medianKeyIndex] = null;

            // Copy the srcNode's elements into dstNode.
            for (i = 0; i < srcNode.mNumKeys; i++) {
                dstNode.mKeys[i] = srcNode.mKeys[i];
                dstNode.mObjects[i] = srcNode.mObjects[i];
                if (!srcNode.mIsLeafNode) {
                    dstNode.mChildNodes[i] = srcNode.mChildNodes[i];
                }
            }
            if (!srcNode.mIsLeafNode) {
                dstNode.mChildNodes[i] = srcNode.mChildNodes[i];
            }
        } else {
            // Clear the median key (element).
            medianKeyIndex = dstNode.mNumKeys;
            dstNode.mKeys[medianKeyIndex] = 0;
            dstNode.mObjects[medianKeyIndex] = null;

            // Copy the srcNode's elements into dstNode.
            int offset = medianKeyIndex + 1;
            int i;
            for (i = 0; i < srcNode.mNumKeys; i++) {
                dstNode.mKeys[offset + i] = srcNode.mKeys[i];
                dstNode.mObjects[offset + i] = srcNode.mObjects[i];
                if (!srcNode.mIsLeafNode) {
                    dstNode.mChildNodes[offset + i] = srcNode.mChildNodes[i];
                }
            }
            if (!srcNode.mIsLeafNode) {
                dstNode.mChildNodes[offset + i] = srcNode.mChildNodes[i];
            }
        }
        dstNode.mNumKeys += srcNode.mNumKeys;
        return medianKeyIndex;
    }

    // Move the key from srcNode at index into dstNode at medianKeyIndex. Note that the element at index is already empty.
    void moveKey(Node srcNode, int srcKeyIndex, int childIndex, Node dstNode, int medianKeyIndex) {
        dstNode.mKeys[medianKeyIndex] = srcNode.mKeys[srcKeyIndex];
        dstNode.mObjects[medianKeyIndex] = srcNode.mObjects[srcKeyIndex];
        dstNode.mNumKeys++;

        srcNode.remove(srcKeyIndex, childIndex);

        if (srcNode == mRootNode && srcNode.mNumKeys == 0) {
            mRootNode = dstNode;
        }
    }

    public Object searchRange(int key1, int key2) {
        return search(mRootNode, key1);
    }

    public Object search(int key) {
        return search(mRootNode, key);
    }

    // Recursive search method.
    public Object search(Node node, int key) {
        int i = 0;
        while (i < node.mNumKeys && key > node.mKeys[i]) {
            i++;
        }
        if (i < node.mNumKeys && key == node.mKeys[i]) {
            if (node.isfastDeleted[i]) {
                return null;
            }
            return node.mObjects[i];
        }
        if (node.mIsLeafNode) {
            return null;
        } else {
            return search(node.mChildNodes[i], key);
        }
    }

    public Node searchForNode(int key) {
        return searchForNode(mRootNode, key);
    }

    // Recursive search method.
    public Node searchForNode(Node node, int key) {

        int i = node.mNumKeys - 1;
        while (i >= 0 && key < node.mKeys[i]) {

            i--;

        }
        if (i >= 0 && node.mKeys[i] == key && !node.isremotenode) {
            return node;
        }
        if (!node.isremotenode) {
            i++;
        }

        if (i < node.mNumKeys && key == node.mKeys[i] && !node.isremotenode) {
            return node;
        }
        if (node.mIsLeafNode) {
            return node;
        } else {
            Node n = null;

            n = searchForNode(node.mChildNodes[i], key);

            return n;
        }
    }

    public Object search2(int key) {
        return search2(mRootNode, key);
    }

    // Iterative search method.
    public Object search2(Node node, int key) {
        while (node != null) {
            int i = 0;
            while (i < node.mNumKeys && key > node.mKeys[i]) {
                i++;
            }
            if (i < node.mNumKeys && key == node.mKeys[i]) {
                return node.mObjects[i];
            }
            if (node.mIsLeafNode) {
                return null;
            } else {
                node = node.mChildNodes[i];
            }
        }
        return null;
    }

    public void fastDelete(Node node, int key) {
        int i = 0;

        while (i < node.mNumKeys && key > node.mKeys[i]) {
            i++;
        }
        if (i < node.mNumKeys && key == node.mKeys[i]) {
            if (node.mIsLeafNode) {
                node.remove(i, LEFT_CHILD_NODE);
            } else {
                node.isfastDeleted[i] = true;

            }
        }

    }

    public void printOutWholetree(int key) {
        printOutWholetree(mRootNode, 1, key);
    }

    // Iterative search method.
    public void printOutWholetree(Node node, int level, int key) {
        int foundindex = -3;
        if (key == -1) {
            foundindex = -1;
        }
        if (node != null) {

            for (int i = 0; i < node.mKeys.length; i++) {
                if (i < (node.mNumKeys + 1)) {
                    if (node.mKeys[i] == key && key != -2) {
                        key = -1;
                        foundindex = i;
                    }
                }
            }
            if (key == -1) {
                System.out.println("n:" + node.toString() + " level:" + level);
                for (int i = 0; i < node.mKeys.length; i++) {
                    if (i < (node.mNumKeys + 1)) {
                        if ((key == -1)) {
                            System.out.println(i + ":" + node.mKeys[i]);
                        }
                    }
                }
                System.out.println("*************************");
            }
            /*while (i < node.mNumKeys && key > node.mKeys[i]) {
             i++;
             }
             if (i < node.mNumKeys && key == node.mKeys[i]) {                                
             return node.mObjects[i];
             }*/
            if (node.mIsLeafNode) {
                //return null;
            } else {
                for (int c = 0; c < node.mChildNodes.length; c++) {
                    if (c < (node.mNumKeys + 1)) {
                        Node cnode = node.mChildNodes[c];

                        if (c == foundindex || c == (foundindex + 1)) {
                            //    printOutWholetree(cnode, level + 1, key);
                        }
                        if (key == -1) {
                            printOutWholetree(cnode, level + 1, key);
                        }
                    }

                }
            }

        }
        //return null;
    }

    public ArrayList<Integer> getRange(int min, int max) {
        ArrayList<Integer> results = new ArrayList<Integer>();
        getRange(mRootNode, 1, min, max, results);
        return results;
    }

    // Iterative search method.
    public void getRange(Node node, int level, int min, int max, ArrayList<Integer> results) {
        //
        if (node != null) {
            for (int i = 0; i < node.mNumKeys; i++) {
                if (!node.mIsLeafNode) {
                    if (i < node.mChildNodes.length) {
                        Node cnode = node.mChildNodes[i];
                        getRange(cnode, level + 1, min, max, results);
                    }
                }
                if (node.mKeys[i] >= min && node.mKeys[i] <= max && !node.isremotenode) {

                    results.add(node.mKeys[i]);

                    
                    
                }
                if (node.mKeys[i] >= max && !node.isremotenode) {
                    return;
                }
                if (!node.mIsLeafNode && (i == node.mNumKeys)) {// && ((node.mChildNodes.length +1) == node.mKeys.length)) {
                    if (node.mChildNodes[i + 1] != null) {
                        Node cnode = node.mChildNodes[i + 1];
                        getRange(cnode, level + 1, min, max, results);
                    }
                }
            }
        }
    }

    private boolean update(Node node, int key, Object object) {
        while (node != null) {
            int i = 0;
            while (i < node.mNumKeys && key > node.mKeys[i]) {
                i++;
            }
            if (i < node.mNumKeys && key == node.mKeys[i]) {
                //STAC: This makes it very difficult to do analysis
                if (node.mObjects == null) {
                    return false;
                }
                node.mObjects[i] = object;
                return true;
            }
            if (node.mIsLeafNode) {
                return false;
            } else {
                node = node.mChildNodes[i];
            }
        }
        return false;
    }

     void recurseBTree(List<Integer> res, int min, int max,Node node, int order) {
  
        if (node != null) {
            if (node.mIsLeafNode) {
                for (int i = 0; i < node.mNumKeys; i++) {
                 //string += node.mObjects[i] + ", ";
                 if(node.mKeys[i]>=min && node.mKeys[i]<=max)
                    res.add(node.mKeys[i]);
                 }
                
            } else {
                int i;
                for (i = 0; i < node.mNumKeys; i++) {
                    //string += node.mObjects[i] + "order:"+ order+ ", \n";
                    if(node.mKeys[i]>=min && node.mKeys[i]<=max){
                        res.add(node.mKeys[i]);
                    }
                    recurseBTree(res,  min,  max,node.mChildNodes[i], order + 1);
                    
                    //string += order + ", ";
                }
                recurseBTree(res,  min,  max,node.mChildNodes[i], order + 1);
            }
        }
    
    }
    
    String printBTree(Node node, int order) {
        String string = "";
        if (node != null) {
            if (node.mIsLeafNode) {
                for (int i = 0; i < node.mNumKeys; i++) {
                 string += node.mObjects[i] + ", ";
                 }
                string +="\n";
            } else {
                int i;
                for (i = 0; i < node.mNumKeys; i++) {
                    string += node.mObjects[i] + "order:"+ order+ ", \n";
                    string += printBTree(node.mChildNodes[i], order + 1);
                    
                    //string += order + ", ";
                }
                string += printBTree(node.mChildNodes[i], order + 1);
            }
        }
        return string;
    }
    // Inorder walk over the tree.
    String printBTreeX(Node node, int order) {
        String string = "";
        if (node != null) {
            if (node.mIsLeafNode) {
                /*for (int i = 0; i < node.mNumKeys; i++) {
                 string += node.mObjects[i] + ", ";
                 }*/
            } else {
                int i;
                for (i = 0; i < node.mNumKeys; i++) {
                    string += printBTree(node.mChildNodes[i], order + 1);
                    //string += node.mObjects[i] + "order:"+ order+ ", ";
                    string += order + ", ";
                }
                string += printBTree(node.mChildNodes[i], order + 1);
            }
        }
        return string;
    }

    public String toString() {
        return printBTree(mRootNode, 0);
    }
    
    public List<Integer> toList(int min, int max) {
        List<Integer> res = new ArrayList<Integer>();
         recurseBTree(res,min,max,mRootNode, 0);
         Collections.sort(res);
         return res;
    }

    void validate() throws Exception {
        List<Integer> array = getKeys(mRootNode);
        for (int i = 0; i < array.size() - 1; i++) {
            if (array.get(i) >= array.get(i + 1)) {
                throw new Exception("B-Tree invalid: " + array.get(i) + " greater than " + array.get(i + 1));
            }
        }
    }

    // Inorder walk over the tree.
    List<Integer> getKeys(Node node) {
        //List<Integer> array = new Vector<Integer>(1);

        List<Integer> array = new ArrayList<Integer>();

        if (node != null) {
            if (node.mIsLeafNode) {
                for (int i = 0; i < node.mNumKeys; i++) {
                    array.add(node.mKeys[i]);
                }
            } else {
                int i;
                for (i = 0; i < node.mNumKeys; i++) {
                    array.addAll(getKeys(node.mChildNodes[i]));
                    array.add(node.mKeys[i]);
                }
                array.addAll(getKeys(node.mChildNodes[i]));
            }
        }
        return array;
    }
}
