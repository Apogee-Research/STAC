package server;
/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import distfilesys.system.DSystem;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import index.BTree;

import distfilesys.system.DistributedFile;
import distfilesys.system.remote.DFileHandle;
import distfilesys.system.remote.DSystemHandle;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import server.util.CheckRestrictedID;
import static server.util.Utils.restore;

public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    //private static final Random random = new Random();
    BTree btree = new BTree(10);
    CheckRestrictedID restricted = new CheckRestrictedID();
    
    public static Integer IDMIN = 100000;
    public static Integer IDMAX = 40000000;
    
    public UDPServerHandler() throws IOException {

        //STAC: Initialize the database
        //restore(btree, "datasetbig.dump", restricted);
        restore(btree, "dataset.dump", restricted);
        
        String toString = btree.toString();
        System.out.println(toString);
        //ArrayList<Integer> range = btree.getRange(0, 39462421);
        
        //STAC: Set the optimized insert flag, 
        //this flag causes non-leaf nodes to add more items before spliting.
        //The goal is to make inserts faster at runtime by slowing the cascading property of splits.
        //The non-leafs stil split, but they grow larger than leaves before doing so.  
        //The leaf nodes still split at normal rates. Keeping small leaf nodes allows for faster inserts due to
        //requirements for shuffling the array contents. 
        btree.optimizedinserts = true;

        System.out.println("UDP Server Up and Running");
    }

    int totaloperations = 0;
    
    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        ByteBuf bos = null;
        ByteBufAllocator alloc = PooledByteBufAllocator.DEFAULT;

        boolean sent = false;
        byte t = packet.content().getByte(0);

        totaloperations++;
        //System.out.println("operations:"+totaloperations);
        
        //STAC:The server request handler -- 
        //This code cause most of the noise that makes the side channel difficult to detect
        //even the harness has difficultly on some environments -- though not on the NUC :)
        //Everything here is kept outside of functions when possible, 
        //even adding a function call added too much noise on a MAC laptop with Sun JDK 
        //-- although, again, that was not the case on the NUC with open JDK.
        try {
            switch (t) {
                case 1: {
                    Integer key = packet.content().getInt(1);
                    //long start = System.nanoTime();

                    //STAC: Commented out transactions. I thought about about doing auto transactions -- transactions make the split slower, more
                    //work is required to back up a node that splits than a simple insert to a leaf.
                    //However,  timing seems long enough in later versions to allow for the elimination of transactions altogether
                    //I think this  is good: the transactions allow for a potentially easier to detect --
                    //but more complex to implement side channel -- maybe too hard for first round. (Detection of the side channel is easier 
                    //because the same action can be repeated and timings averaged and outliers removed, so a closer timings can be achieved)
                    //
                    //Future thoughts: Another channel could result if we have auto-commit transactions occur in such a way that
                    //a user-induced failure results in a rollback after spliting or not splitting.
                    //For example: setting the SET CONSTRAINTS ALL DEFERRED in oracle! 
                    //deferred constraints can save time on batch inserts -- this makes transactions too tempting to burn in this iteration
                    //Potential code reiew discussion topic: LEAVE TRANSACTIONS OUT FOR THIS VERSION
                    //btree.beginTransaction();
                    //STAC: do the insert -- NOTICE I ONLY INSERT THE KEY, NO VALUE to keep timings predictable
                    //Values get inserted in a  subsequent request
                    //Inserting behavior no longer causes the side channel, so more checks are ok to add
                    boolean b = true;

                    DSystemHandle sys = new DSystemHandle("127.0.0.1", 6666);
                    DFileHandle fhlog = new DFileHandle("insertkey.log", sys);

                    fhlog.setContents("INSERT KEY OCCURRED:" + key);
                    fhlog.store(null, null);

                    System.out.println("request add key:"+key);
                    if (this.restricted.isRestricted(key)) {
                        System.out.println("don't add key:"+key);
                        //return normal status
                        b = true;
                    } else if(!(key>= IDMIN && key<=IDMAX)){
                        System.out.println("not a valid key:"+key);
                        b = false;
                    }
                    else 
                    {
                        System.out.println("add key:"+key);
                        b = btree.add(key, null, false);
                    }
                    //btree.commit();
                    //long end = System.nanoTime() -start;
                    //System.out.println("key:"+key);
                    //System.out.println("time:"+end);
                    byte b_b = b ? (byte) 1 : (byte) 0;

                    //STAC: No error code;  just send back 1 always when we get this far, unless an exception is thrown
                    bos = alloc.directBuffer(1);
                    bos.writeByte(b_b);

                    //ctx.write(new DatagramPacket(bos, packet.sender()));
                    //ctx.flush();
                    //sent = true;
                }
                break;
                //DON'T ALLOW USER TO SET THIS ANYMORE
                /*case 2: {
                 byte B = packet.content().getByte(1);
                 btree.optimizedinserts = (B != 0);
                 byte b_b = (byte) 1;
                 bos = alloc.directBuffer(1);
                 bos.writeByte(b_b);
                 }
                 break;*/

                //STAC: I'm not currently making the transaction code unavailable for this one, so next 3 handlers are commented out
                //-- they work and can added back in 
                /*case 3: {
                 //BEGIN NEW TRANSACTION
                 btree.beginTransaction();
                 byte b_b = (byte) 1;
                 bos = alloc.directBuffer(1);
                 bos.writeByte(b_b);
                 }
                 break;

                 case 4: {
                 //COMMIT TRANSACTION
                 btree.commit();
                 byte b_b = (byte) 1;
                 bos = alloc.directBuffer(1);
                 bos.writeByte(b_b);
                 }
                 break;

                 case 5: {
                 //ROLLBACK TRANSACTION
                 Integer L = packet.content().getInt(1);
                 btree.rollback();
                 byte b_b = (byte) 1;
                 bos = alloc.directBuffer(1);
                 bos.writeByte(b_b);
                 }
                 break;*/
                /*case 6: {
                 Integer key = packet.content().getInt(1);
                 boolean b = btree.add(key, key, false);
                 byte b_b = b ? (byte) 1 : (byte) 0;
                 //ByteBuf bos = Unpooled.buffer(5);
                 bos = alloc.directBuffer(1);
                 bos.writeByte(b_b);
                 }
                 break;*/
                /*case 7: {
                 Object res = btree.searchForNode(374802);
                 btree.printNode((BTree.Node) res);
                 byte b_b = (byte) 1;
                 bos = alloc.directBuffer(1);
                 bos.writeByte(b_b);
                 break;
                 }*/
                case 8: {
                    //STAC:Just search over a range of keys and get all the keys in that range
                    Integer min = packet.content().getInt(1);
                    Integer max = packet.content().getInt(5);
                    DSystemHandle sys = sys = new DSystemHandle("127.0.0.1", 6666);

                    List<String> filestoCheck = new ArrayList<String>();
                    DFileHandle fh1 = new DFileHandle("config.security", sys);

                    filestoCheck.add("config.security");
                    ////System.out.println("-1");                    

                    //String retrievedcontents1 = fh1.retrieve();
                    //This is the Index search
                    //ArrayList<Integer> range = btree.getRange(min, max);
                    List<Integer> range = btree.toList(min, max);
                    
                    ////System.out.println("-1.1");
                    if (range.size() > 0 && this.restricted.isRestricted(range.get(0))) {
                        //DFileHandle.getContents(filestoCheck.toArray(new String[0]), sys);
                        ////System.out.println("-1.2");
                    } else {
                        //String retrievedcontents1 = fh1.retrieve();
                        String contents = DFileHandle.getContents(filestoCheck.toArray(new String[0]), sys);
                        filestoCheck = new ArrayList<String>();
                        ////System.out.println("-1.3");
                    }
                    ////System.out.println("-2");
                    AccessTracker at = new AccessTracker();
                    AccessTracker atx = null;
                    /*Socket socket = new Socket(DSystem.ADDRESS, DSystem.PORT);
                     ObjectOutputStream out = new ObjectOutputStream(socket
                     .getOutputStream());*/

                    //Iterator<Integer> it = range.iterator();
                    int ind = 0;
                    while (ind < range.size()) {
                        try {
                            Integer nextkey = range.get(ind);

                            //STAC:If the key is restricted, do not return it
                            //This is a potentially trivial side channel since more work does occur here (but very little)
                            //when a secret value falls within the search range
                            //String out = "<>" + nextkey.toString() + "<>" + "of" + range.size();
                            //System.out.println(out);
                            bos = alloc.directBuffer(4);
                            bos.writeInt(nextkey);
                            if (!this.restricted.isRestricted(nextkey)) {

                                if (sys == null) {
                                    sys = new DSystemHandle("127.0.0.1", 6666);
                                }
                                //// System.out.println("-3");
                                DFileHandle fhlog = new DFileHandle("lastaccessinfo.log", sys);

                                fhlog.setContents("SEARCH NONRESTRICTED KEY OCCURRED:" +Integer.toString(nextkey));
                                //fhlog.setContents(Integer.toString(nextkey));
                                fhlog.store(null, null);
                                ctx.writeAndFlush(new DatagramPacket(bos, packet.sender()));
                                bos.clear();
                                at.add("lastaccessinfo.log", Integer.toString(nextkey), nextkey);
                                ind++;

                            } else {
                                // System.out.println("restricted keyv:" + nextkey);
                                //bos.clear();
                                atx = new AccessTracker();
                                atx.add("lastaccessflag.log", "SEARCH ON RESTRICTED KEY OCCURRED:" + nextkey, nextkey);
                                throw new RestrictedAccessException();

                            }
                        } catch (RestrictedAccessException rae) {

                            //System.out.println("restricted key exeption:" + ind);
                            Integer getkey = range.get(ind);
                            //System.out.println("restricted key exeption:" + getkey);
                            while (this.restricted.isRestricted(getkey) && ind < range.size()) {
                                //System.out.println("restricted key:" + getkey);
                                if (sys == null) {
                                    sys = new DSystemHandle("127.0.0.1", 6666);
                                }
////System.out.println("-4");                                
                                //DFileHandle fhlog = new DFileHandle("lastaccess.log"+getkey, sys);

                                //fhlog.setContents("SEARCH ON RESTRICTED KEY OCCURRED:"+getkey);
                                /*out.close();
                                 socket.close();
                                 socket = new Socket(DSystem.ADDRESS, DSystem.PORT);
                                 out = new ObjectOutputStream(socket
                                 .getOutputStream());*/
                               // fhlog.store(null,null);
                                /*BTree.Node node = btree.searchForNode(getkey);
                                 String data = "";
                                 if (node != null) {
                                    
                                 int index = 0;
                                 while (index < node.mNumKeys) {
                                 int keyv = node.mKeys[index];
                                 if (keyv == getkey) {
                                 //STAC: Here is where we update the value
                                 data = (String) node.mObjects[index];
                                 }
                                 index++;
                                 }
                                 }*/
                                /*filestoCheck.add(getkey.toString());
                                 DFileHandle fh = new DFileHandle(getkey.toString(), sys);
                                
                                 String retrievedcontents = fh.retrieve();
                                 if (retrievedcontents != null) {
                                 int indexOf = retrievedcontents.indexOf("permoverride=");
                                 if (indexOf > -1) {
                                 String permoverride = retrievedcontents.substring(indexOf, retrievedcontents.length());
                                 if (permoverride.equals("1")) {
                                            
                                 this.restricted.remove(getkey);
                                 bos = alloc.directBuffer(4);
                                 bos.writeInt(getkey);
                                 ctx.writeAndFlush(new DatagramPacket(bos, packet.sender()));
                                 bos.clear();
                                 } 
                                 }
                                 }*/
                                ind++;
                                if (ind < range.size()) {
                                    getkey = range.get(ind);
                                }
                            }
                            //String contents = "NJVNNKNKV";//
                            /*String contents = DFileHandle.getContents(filestoCheck.toArray(new String[0]), sys);
                             if(contents!=null && contents.contains("ACCESSLEVEL:OVERRIDE")){
                                
                                
                             ctx.writeAndFlush(new DatagramPacket(bos, packet.sender()));
                             bos.clear();
                             ind++;
                             } else {
                             bos.clear();
                             }*/
                            //System.out.println(contents);
                        } finally {
                            if (atx != null) {
                                System.out.println("Cleaning resources");

                                //Thread.sleep(8);
                                atx.clean();
                                atx = null;
                                ////System.out.println("-6");                             
                            }
                        }
                    }
                    //// System.out.println("-3");  
                    at.clean();
                    sys = new DSystemHandle("127.0.0.1", 6666);

                    DFileHandle fh = new DFileHandle("lastaccess.log", sys);

                    fh.setContents("SEARCH[" + min + ":" + max + "]");
                    fh.store(null, null);

                    bos = alloc.directBuffer(4);
                    bos.writeInt(-8);
                    System.out.println("search done");
                    break;
                }
                case 9: {
                    //STAC: Just set the value of the key
                    int pos = 1;
                    Integer key = packet.content().getInt(pos);
                    pos += Integer.SIZE >> 3;
                    Integer sizeofdata = packet.content().getInt(pos);
                    pos += Integer.SIZE >> 3;
                    BTree.Node node = btree.searchForNode(key);
                    StringBuffer data = new StringBuffer();
                    int startpos = pos;
                    for (; pos < startpos + sizeofdata * (Character.SIZE >> 3); pos += (Character.SIZE >> 3)) {
                        char c = (char) packet.content().getChar(pos);
                        data.append(c);

                    }
                    if (node != null) {

                        int index = 0;
                        while (index < node.mNumKeys) {

                            int keyv = node.mKeys[index];
                            if (keyv == key) {
                                //STAC: Here is where we update the value
                                node.mObjects[index] = data.toString();
                                if (node.instantSearch != null && node.instantSearch.contains(index)) {
                                    node.instantSearch.put(key, data);
                                }
                            }
                            index++;
                        }

                    }
                    byte ret = 1;
                    bos = alloc.directBuffer(1);
                    bos.writeByte(ret);
                }
                break;
                case 10: {
                    Integer key = packet.content().getInt(1);
                    Integer val = -8;
                    //STAC:DOn'T SEND BACK VALUES IF THEY ARE RESTRICTED -- Also contains a very trivial side-channel like case 8 above
                    if (!this.restricted.isRestricted(key)) {

                        String valstr = null;
                        BTree.Node node = btree.searchForNode(key);

                        boolean found = false;
                        if (node != null) {
                            int index = 0;
                            if (node.instantSearch != null) {
                                //STAC: this an unnecessary, but not completely useless, 'optimization'.  Filling up this hashmap takes a 
                                //bit of time and only occurs during a split -- this just makes it look like the HashMap has a reason to exist and, well, it is probably faster
                                if (node.instantSearch.containsKey(key)) {
                                    //This must be a string
                                    valstr = (String) node.instantSearch.get(key);
                                    if (valstr.contains("null")) {
                                        found = false;
                                    }
                                }
                            }
                            //STAC:Ok now  get the value from the tree
                            if (!found) {
                                while (!found && index < node.mNumKeys) {

                                    int keyv = node.mKeys[index];
                                    if (keyv == key) {
                                        found = true;
                                        Object ret = node.mObjects[index];
                                        if (ret instanceof Integer) {
                                            val = -8;//(Integer) node.mObjects[index];
                                        }
                                        if (ret instanceof String) {
                                            valstr = (String) node.mObjects[index].toString();
                                            found = true;
                                        }
                                    }
                                    index++;
                                }
                            }
                        }
                        //STAC:Send back the data we just found
                        if (found) {
                            bos = alloc.directBuffer((Integer.SIZE >> 3) + valstr.length() * (Character.SIZE >> 3));
                            StringBuffer data = new StringBuffer(valstr);

                            for (int i = 0; i < data.length(); i++) {

                                bos.writeByte(data.charAt(i));
                            }
                        } else {
                            bos = alloc.directBuffer((Integer.SIZE >> 3));
                        }
                    }
                    bos.writeByte(val);

                }
                break;
                case 11: {

                    //STAC:Put the file contents related to a key into the distributed store
                    Integer sizeofn = packet.content().getInt(1);
                    StringBuffer name = new StringBuffer();

                    int pos = 5;
                    int check = (pos + sizeofn * (Character.SIZE >> 3));
                    for (; pos < check; pos += 2) {
                        char c = (char) packet.content().getChar(pos);
                        name.append(c);

                    }
                    StringBuffer data = new StringBuffer();
                    Integer sizedata = packet.content().getInt(pos);
                    pos += 4;
                    check = (pos + sizedata * (Character.SIZE >> 3));
                    for (; pos < check; pos += 2) {
                        char c = (char) packet.content().getChar(pos);
                        data.append(c);

                    }

                    DSystemHandle sys = new DSystemHandle("127.0.0.1", 6666);

                    DFileHandle fh = new DFileHandle(name.toString(), sys);

                    fh.setContents(data.toString());
                    fh.store(null, null);

                    byte b_b = (byte) 1;
                    bos = alloc.directBuffer(1);
                    bos.writeByte(b_b);
                    //String retrieve = fh.retrieve();
                    //System.out.println(retrieve);

                }
                break;
                //Commenting this out -- It could lead to more side channels!!!!!
                /*case 12: {
                 Integer key = packet.content().getInt(1);
                 btree.delete(key);
                 byte b_b = 1;//
                 //ByteBuf bos = Unpooled.buffer(5);
                 bos = alloc.directBuffer(1);
                 bos.writeByte(b_b);
                 }
                 break;*/
                case 13: {
                    //STAC: Get the contents from the distributed store
                    DSystemHandle sys = new DSystemHandle("127.0.0.1", 6666);
                    int pos = 1;
                    Integer sizeofn = packet.content().getInt(pos);
                    pos += (Integer.SIZE >> 3);
                    StringBuffer name = new StringBuffer();

                    int check = pos + sizeofn * (Character.SIZE >> 3);
                    for (; pos < check; pos += 2) {
                        char c = (char) packet.content().getChar(pos);
                        name.append(c);
                    }

                    int indexOfVal = name.indexOf(":");

                    String keystr = name.substring(0, indexOfVal);
                    int key = Integer.parseInt(keystr);
                    if (!this.restricted.isRestricted(key)) {

                        DFileHandle fh = new DFileHandle(name.toString(), sys);

                        String retrievedcontents = fh.retrieve();
                        //send back the file contents
                        bos = alloc.directBuffer(retrievedcontents.length() * (Character.SIZE >> 3));
                        //bos.writeInt(retrievedcontents.length());
                        for (int i = 0; i < retrievedcontents.length(); i++) {

                            bos.writeByte(retrievedcontents.charAt(i));
                        }
                    }

                }
                break;

                default:
                    byte b_b = (byte) 0;
                    bos = alloc.directBuffer(1);
                    bos.writeByte(b_b);
                    break;
            }
        } catch (Exception e) {
            //Just catch everything, runtime all of it
            byte b_b = (byte) -1;
            bos = alloc.directBuffer(1);
            bos.writeByte(b_b);
            Logger.getLogger(UDPServerHandler.class.getName()).log(Level.SEVERE, e.getMessage(), e);

        }
        //ctx.write(null);//disconnect();//close();
        //if(!sent)
        ctx.writeAndFlush(new DatagramPacket(bos, packet.sender()));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        //ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // We don't close the channel because we can keep serving requests.
    }

    private static class RestrictedAccessException extends Exception {

        public RestrictedAccessException() {
        }
    }
}
