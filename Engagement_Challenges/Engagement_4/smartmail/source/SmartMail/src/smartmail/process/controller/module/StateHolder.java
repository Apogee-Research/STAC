/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.process.controller.module;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import smartmail.process.controller.module.seqfile.ReaderWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
//import com.google.common.collect.

/**
 *
 * @author burkep
 */
public class StateHolder implements ReaderWriter {

    public Multimap sfmap;
    Multimap mmap;
    Multimap rmap;
    Multimap tomap;
    Multimap frommap;
    Mapper mapper;
    Reducer reducer;

    public static final int partitionsize = 4;

    public StateHolder() {

        sfmap = Multimaps.synchronizedMultimap(ArrayListMultimap.create());

        mmap = Multimaps.synchronizedMultimap(ArrayListMultimap.create());
        rmap = Multimaps.synchronizedMultimap(ArrayListMultimap.create());

    }

    public void sfclear() {
        sfmap.clear();

    }

    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public void setReducer(Reducer reducer) {
        this.reducer = reducer;
    }

    public void callMapper() throws IOException {
        System.out.println("Call Mapper");
        callDumper("Mapper");
        Multiset keys = mmap.keys();

    }

    public void callReducer() throws IOException {
        callDumper("Reducer");
    }

    private void callDumper(String stage) throws IOException {
        long startTime = System.currentTimeMillis();
        if (stage.equals("Mapper")) {
            frommap = sfmap;
            tomap = mmap;
        }
        if (stage.equals("Reducer")) {
            frommap = mmap;
            tomap = rmap;
        }

        Set keys = frommap.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            Object nextkey = it.next();
            Collection nextcoll = frommap.get(nextkey);
            Iterator itv = nextcoll.iterator();
            if (stage.equals("Mapper")) {
                while (itv.hasNext()) {
                    Object next = itv.next();
                    mapper.map(nextkey, next, this, Reporter.NULL);
                }
            } else if (stage.equals("Reducer")) {
                reducer.reduce(nextkey, itv, this, Reporter.NULL);
            }
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
    }

    @Override
    public void collect(Object k, Object v) throws IOException {
        tomap.put(k, v);
    }

    int pcurr = 0;
    int vadded = 0;

    @Override
    public void append(Writable key, Writable val) {

        sfmap.put(key, val);
    }

    @Override
    public void close() throws IOException {
    }

    public Multimap getOutput() {

        return tomap;
    }

}
