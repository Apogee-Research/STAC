/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.process.controller.module;

import smartmail.logging.module.ObjSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Writable;
import smartmail.datamodel.EmailEvent;
import smartmail.datamodel.MessageWord;

/**
 *
 * @author user
 */
public class Partitioner {

    List<List<BytesWritable>> sfmaplist;
    private final int partitionsize;
    StateHolder sfile;

    public Partitioner(int psize, StateHolder sfile) {
        partitionsize = psize;
        this.sfile = sfile;
        initseqfiles();
    }

    private void initseqfiles() {

        Collection values = sfile.sfmap.values();

        List<BytesWritable> valuesl = new ArrayList(values);
        sfmaplist = chopped(valuesl, partitionsize);

    }

    static List<List<BytesWritable>> chopped(List<BytesWritable> list, final int L) {

        //STAC: The list partitioner function
        List<List<BytesWritable>> parts = new ArrayList<List<BytesWritable>>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<BytesWritable>(
                    list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
    }

    List<BytesWritable> getPartition(int i) {
        //BytesWritable next = itrtr.next();
        //MessageWord deobj = (MessageWord) ObjSerializer.deSerializeObj(next);

        return sfmaplist.get(i);
    }
}
