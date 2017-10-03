/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package index;

import java.util.ArrayDeque;
import java.util.Deque;
import index.BTree.Node;


/**
 *
 * @author user
 */
public class TransactionStack {

    Deque<Undo> t_stack = null;
    BTree btree;

    public boolean is_active = false;

    public TransactionStack(BTree btree) {
        t_stack = new ArrayDeque<Undo>(1000);
        this.btree = btree;

    }

    public static int INSERT;
    public static int DELETE;
    public static int UPDATE;

    void begin() {
        while (t_stack.poll() != null) {
        }
        is_active = true;
    }

    public class Undo {

        int type;
        int key;
        Node n;
        Node nparent;
        Node newn;
        Node newnparent;

        public Undo(int type, Node n, Node nparent, int key) {

            this.type = type;
            this.n = n;
            this.key = key;
            this.nparent = nparent;
            newn = this.newn;
            newnparent = this.newnparent;

            prepare();

        }

        public void prepare() {
            if (type == INSERT) {
                //STAC:n should only be NOT null during a split -- so transactions for splits are slower
                if (n != null) {
                    newn = n.copy(null);
                    newnparent = nparent.copy(null);

                } else {

                }
            }
        }

        public void undo() {

            if (type == INSERT) {

                if (n != null) {
                    //STAC:rollback is slower for splits
                    nparent.copyin(newnparent);
                    n.copyin(newn);

                } else {
                    //STAC:quick for non-splits
                    btree.delete(key);
                }
            } /*else if (type == DELETE) {

            } else if (type == UPDATE) {

            }*/

        }

    }

    public void addInsert(Node n, Node parent, int key) {

        if (is_active) {
            //System.out.println("ADDTRANSACTION --BELOW IS WHAT WE ARE BACKING UP:" + key);
            //if(n!=null)
           //     btree.printOutWholetreeX(-1);
            Undo undo = new Undo(INSERT, n, parent, key);
            t_stack.addLast(undo);
        }

    }

    public void rollback() {
        //try {
            //System.out.println("ROLLBACK BEGIN!!!!!~~~~~~~~!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            //thebtree.printOutWholetreeX(-1);
            while (t_stack.peekLast() != null) {
              //  System.out.print("ROLLBACK -- BELOW IS NEW STATE");//PREVIOUS IS WHAT THE STATE IS BEFORE THE UPCOMING UNDO");
                Undo last = t_stack.removeLast();
                //System.out.println(" last.key" + last.key + " n:" + last.n);
                last.undo();
                //if(last.n!=null)
                //btree.printOutWholetreeX(-1);
            }
       /* } catch (java.lang.NullPointerException e) {
        //    System.out.println(e.getMessage());
            System.exit(2);
        }*/

    }

    public void commit() {

        while (t_stack.poll() != null) {
        }
    }

}
