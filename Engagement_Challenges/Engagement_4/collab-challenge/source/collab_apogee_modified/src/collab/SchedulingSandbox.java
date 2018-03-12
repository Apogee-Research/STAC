/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collab;

import collab.dstructs.objs.NormalUserData;
import collab.dstructs.objs.DataHolder;
import collab.dstructs.objs.AuditorData;
import collab.dstructs.DotNodeCallBack;
import collab.dstructs.PrintNodeCallBack;
import collab.dstructs.TempIndexNode;
import collab.dstructs.TreeNodeCallBack;
import collab.dstructs.objs.DataNode;
import collab.dstructs.objs.MedianNode;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static collab.dstructs.objs.DataNode.NodeMAX;
import collab.utils.LogBuffer;

/**
 *
 * @author user
 */
public class SchedulingSandbox {

    public static final int NOVAL = -1;
    public static final int MAXPLUSONE = 2147483647;
    public TempIndexNode root;
    public static final int ENDVAL = -2;
    private boolean issboxinsertionmodeenabled;
    //TODO: TAKE THSI OUT
    //public boolean split = false;
    public LogBuffer log;

    public HashMap<Integer, DataHolder> thedata;
    //Apogee Added
    public boolean checkStatus = false;

    public static SchedulingSandbox populateSandbox(EventResultSet eres) throws DuplicateKeyException, InvalidValueException {

        List<Integer> resasints = eres.get();
        
        /*if(resasints.size()>2000){
            throw new InvalidValueException("Your at your max number of events, take a break already");
        }*/
                                int[] data = new int[resasints.size()];
                        for (int i = 0; i < resasints.size(); i++) {
                            data[i] = resasints.get(i);
                        }
        return new SchedulingSandbox(data);

    }

    private SchedulingSandbox() {

        //STAC: The log aids increasing the variation in time for splits -- splits log more data
        log = new LogBuffer("logs/" + System.currentTimeMillis() + ".log");

        //Create the root node in the tree
        root = new DataNode();
        initnode((DataNode) root);

        //other information about the events goes here
        thedata = new HashMap<Integer, DataHolder>();

    }

    public SchedulingSandbox(int[] init_eventids) throws DuplicateKeyException {

        this();
        add(init_eventids);

    }

    public void initnode(DataNode dn) {
        int[] eventids = new int[NodeMAX];
        Arrays.fill(eventids, NOVAL);
        eventids[NodeMAX - 1] = ENDVAL;
        dn.setUserObject(eventids);

    }

    public void initnode(MedianNode dn) {
        int[] eventids = new int[0];
        Arrays.fill(eventids, MAXPLUSONE);
        dn.setUserObject(eventids);

    }

    public void initSandbox() {
        //This set Sandbox insertion mode
        issboxinsertionmodeenabled = true;
    }

    public void commit() {
        issboxinsertionmodeenabled = false;
    }

    public void add(int[] eventids) throws DuplicateKeyException {

        for (int i = 0; i < eventids.length; i++) {
            add(eventids[i], new NormalUserData());
        }

    }

    public boolean add(int eventid, DataHolder normalUserData) throws DuplicateKeyException {



        SchedulingSandbox.this.addhelper(eventid);

        thedata.put(eventid, normalUserData);

        return true;

    }

    private void addhelper(int eventid) throws DuplicateKeyException {

        DataNode dNode = getDNode(root, eventid);
        if (dNode != null) {
            addhelper(dNode, eventid);
        } else {
            System.out.println("dnode NULL: should not happen");
        }

    }

    private void addhelper(DataNode node, int eventid) throws DuplicateKeyException {

        //This is the list of eventids that are already in the tree
        int[] eventidlist = (int[]) node.getUserObject();

        for (int ind = 0; ind < eventidlist.length; ind++) {
            //if it matches, it is a duplicate
            if (eventidlist[ind] == eventid) {
                //STAC: this will throw exception when we attempt
                //to overwrite the auditor id
                throw new DuplicateKeyException("key:"+eventidlist[ind] + " in index:" +ind);
            }
            //Empty location in the node
            if (eventidlist[ind] == NOVAL) {
                eventidlist[ind] = eventid;
                return;
            }
            //The final and always Empty location in the node
            if (eventidlist[ind] == ENDVAL) {
                //Logging data to cause timing observable
                log.publish("node full");

                //we must sort the list with the inserted value
                eventidlist[ind] = eventid;
                int temp;
                for (int i = 0; i <= ind; i++) {
                    for (int j = 1; j <= ind; j++) {
                        if (eventidlist[j - 1] > eventidlist[j]) {
                            temp = eventidlist[j - 1];
                            eventidlist[j - 1] = eventidlist[j];
                            eventidlist[j] = temp;
                        }
                    }
                }
                //now we take the largest value out of the list
                int lval = eventidlist[ind];
                //put the ENDVAL marker back
                eventidlist[ind] = ENDVAL;
                //Now do our split
                split((DataNode) node);
                //Now add the largest value back, 
                //it will be added to the newly created node
                SchedulingSandbox.this.addhelper(lval);
                //Apogee Added
                checkStatus = true;
            }
            //Apogee Added
            else{checkStatus = false;}
        }
    }

    public void split(DataNode orignode) {

        //TODO: TAKE THIS OUT 
        //System.out.println("SPLIT");
        //TODO: TAKE THIS OUT 
        //split = true;
        //More logging to slow the split time
        log.publish("addnode");
        //Make The new nodes
        DataNode dn1 = new DataNode();
        initnode(dn1);
        log.publish("addnode");
        DataNode dn2 = new DataNode();
        initnode(dn2);
        int[] eventidlist = (int[]) orignode.getUserObject();
        //This loop here just decides which new node each event id gets placed into
        for (int i = 0; eventidlist[i] != ENDVAL; i++) {
            float div = NodeMAX;
            try {
                div = (((float) NodeMAX / (float) i));
            } catch (ArithmeticException e) {
                //System.out.println("Division by zero not Possible!");
            }
            if (div > 2) {
                ((int[]) (dn1.getUserObject()))[i] = eventidlist[i];
            } 
            //This partitions event eventids to the new nodes on the left and right
            else {
                if (NodeMAX % 2 == 1) {
                    ((int[]) (dn2.getUserObject()))[(i - (NodeMAX / 2)) - 1] = eventidlist[i];
                }
                if (NodeMAX % 2 == 0) {
                    ((int[]) (dn2.getUserObject()))[(i - (NodeMAX / 2))] = eventidlist[i];
                }
            }
        }
        TempIndexNode parentnodeoforig = orignode.getParent();
        //We have to update the pointers in teh parent node to point to our
        //new nodes
        //if null, the orignode was the root
        if (parentnodeoforig == null) {
            //This only happens once, when the root node is a DataNode
            //After inserting enough data, the root node now becomes a 
            log.publish("addlevel");
            parentnodeoforig = new MedianNode();
            initnode((MedianNode) parentnodeoforig);
            root = parentnodeoforig;
        } 
        //Assert that this is a MedianNode, though it always should be
        if ( parentnodeoforig instanceof MedianNode) {
            replace(parentnodeoforig, orignode, dn1, dn2);
        }
    }

    public EventResultSet getRange(int min, int max) {

        List<Integer> eventids = new ArrayList<Integer>();
        root.takestep(this, eventids, 1, SchedulingSandbox.MAXPLUSONE - 1, null, new PrintNodeCallBack());

        EventResultSet eres = new EventResultSet(eventids);
        
        return eres;
    }

    public void split(MedianNode tn) {

    }

    //This replaces an old datanode with a median node and updated thepointers 
    //to point to the two new data nodes
    private void replace(TempIndexNode pnode, DataNode orignode, DataNode dn1, DataNode dn2) {

        TempIndexNode[] children = pnode.children();
        int loc = -1;
        if (orignode != null) {
            for (int i = 0; i < children.length; i++) {
                if (children[i].equals(orignode)) {
                    loc = i;
                }
            }
        } 
        
        if (!issboxinsertionmodeenabled) {
            if (loc > -1) {
                pnode.remove(loc);
            }
            pnode.add(dn1);
            pnode.add(dn2);

            makeindex(pnode);

            sort(pnode, (int[]) pnode.getUserObject(), pnode.children());
        } else {
            //TODO: TAKE THESE OUT OR MAKE LOGS
            //System.out.println("childrenum: " + pnode.children().length);
            //System.out.println("pnode.hash: " + pnode.hashCode());
            //Make the median node known to its parent in tree
            MedianNode newmediannode = new MedianNode();
            initnode((MedianNode) newmediannode);
            pnode.children()[loc] = newmediannode;
            newmediannode.parent = pnode;

            newmediannode.add(dn1);
            newmediannode.add(dn2);

            makeindex(newmediannode);

            sort(newmediannode, (int[]) newmediannode.getUserObject(), newmediannode.children());
        }

    }

    private static void sort(TempIndexNode tn, int[] vals, TempIndexNode[] children) {
        TempIndexNode[] newchildren = Arrays.copyOf(children, children.length);
        int i, j, first, temp;
        for (i = vals.length - 2; i > 0; i--) {
            first = 0;   //initialize to subscript of first element
            for (j = 1; j <= i; j++) //locate smallest element between positions 1 and i.
            {
                if (vals[j] > vals[first]) {
                    first = j;
                }
            }
            temp = vals[first];   //swap smallest found with element in position i.
            vals[first] = vals[i];
            vals[i] = temp;
            int cindex = -1;
            for (int k = 0; k < children.length; k++) {
                int[] cvals = (int[]) children[k].getUserObject();
                if (cvals[0] == vals[i]) {
                    cindex = k;
                }
            }
            newchildren[i] = children[cindex];
        }
        tn.setChilren(newchildren);
    }

    public void printTree() {

        List<Integer> ids = new ArrayList<Integer>();
        root.takestep(this, ids, 1, SchedulingSandbox.MAXPLUSONE - 1, null, new PrintNodeCallBack());

        System.out.println("done");
    }

    public void printDot() throws FileNotFoundException {

        DotNodeCallBack dCBack = new DotNodeCallBack();

        PrintWriter dotout = new PrintWriter("tree" + dCBack.outnum + ".dot");
        List<Integer> ids = new ArrayList<Integer>();
        root.takestep(this, ids, 1, SchedulingSandbox.MAXPLUSONE - 1, null, dCBack);

        dotout.println("digraph Nodes {");
//node [shape=box];  gy2; yr2;
        Iterator<String> itm = dCBack.medians.iterator();
        dotout.print("node [shape=box]; ");
        while (itm.hasNext()) {
            String next = itm.next();
            String get = dCBack.mappings.get(next);
            dotout.print(next + "[label=\"" + get + "\"];\n");
//node [shape=box];  gy2; yr2; rg2; gy1; yr1; rg1;
//node [shape=circle,fixedsize=true,width=0.9];  green2; yellow2; red2; safe2; safe1; green1; yellow1; red1;
//gy2->yellow2;
        }
        dotout.println("");
        Iterator<String> itn = dCBack.datan.iterator();
        dotout.print("node [shape=box, color=red]; ");
        while (itn.hasNext()) {
            String next = itn.next();
            String get = dCBack.mappings.get(next);
            dotout.print(next + "[label=\"" + get + "\"];\n");
            //System.out.print(next + "; ");
        }
        dotout.println("");
        Iterator<String> itr = dCBack.relations.iterator();
        while (itr.hasNext()) {
            String next = itr.next();
            dotout.println(next + "; ");
        }
        dotout.println("");

        dotout.println("}");
        dotout.flush();
        dotout.close();

    }

    public void walkTreeCBack(TreeNodeCallBack cb) {

        List<Integer> ids = new ArrayList<Integer>();
        root.takestep(this, ids, 1, SchedulingSandbox.MAXPLUSONE - 1, null, cb);

        System.out.println("done");
    }

    public DataNode getDNode(TempIndexNode n, int val) {
        if (n instanceof DataNode) {
            return getDNode((DataNode) n, val);
        }
        if (n instanceof MedianNode) {
            return getDNode((MedianNode) n, val);
        }
        return null;
    }

    public DataNode getDNode(DataNode n, int val) {
        return n;

    }

    public DataNode getDNode(MedianNode n, int val) {

        if (n == null) {
            if (root instanceof DataNode) {
                return (DataNode) root;
            } else {
                n = (MedianNode) root;
            }
        }

        if (n instanceof MedianNode) {
            int[] childids = (int[]) n.getUserObject();

            for (int i = 0; i < childids.length - 1; i++) {
                int data = childids[i];
                //System.out.println("data:"+ data);

                int datapeek = childids[i + 1];

                if (val >= data && val < datapeek) {
                    TempIndexNode[] children = n.children();
                    TempIndexNode c = children[i];

                    if (c instanceof MedianNode) {

                        //System.out.println("median:"+ c.children().length);
                        c = getDNode((MedianNode) c, val);
                    }
                    if (c instanceof DataNode) {
                        //System.out.println("datanode:"+ c.children().length);
                        return (DataNode) c;
                    }
                }
            }
        }
        return null;
    }

    //Updates the new medians points to point to the values in the children
    private void makeindex(TempIndexNode pnode) {

        TempIndexNode[] childrena = pnode.children();
        int[] newindex = new int[childrena.length + 1];
        newindex[childrena.length] = SchedulingSandbox.MAXPLUSONE;

        for (int i = 0; i < childrena.length; i++) {

            newindex[i] = ((int[]) childrena[i].getUserObject())[0];

        }
        pnode.setUserObject(newindex);
    }

    public void delete(int key) {

        DataNode dNode = getDNode(root, key);

        int[] klist = (int[]) dNode.getUserObject();
        int deleteloc = -1;
        for (int i = 0; i < klist.length; i++) {
            if (klist[i] == key) {
                deleteloc = i;
            }
            for (int ind = 0; ind < klist.length; ind++) {
                //System.out.println("looping");
                if (klist[ind] == NOVAL || klist[ind] == ENDVAL) {
                    int t = klist[ind - 1];
                    klist[deleteloc] = t;
                    klist[ind - 1] = NOVAL;

                    klist[ind] = key;
                    //Inline a little bubble sort here and there just to be confusing
                    int temp;
                    for (int ii = 0; ii < ind; ii++) {
                        for (int j = 0; j < ind; j++) {
                            if (klist[ii] < klist[j + 1]) {
                                temp = klist[j + 1];
                                klist[j + 1] = klist[ii];
                                klist[ii] = temp;
                            }
                        }
                    }
                    return;
                }

            }
        }

    }

}
