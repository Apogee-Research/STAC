/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.process.controller.module.seqfile;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.OutputCollector;

/**
 *
 * @author burkep
 */
public interface ReaderWriter extends OutputCollector, java.io.Closeable {

    public void append(Writable key, Writable val);

}
