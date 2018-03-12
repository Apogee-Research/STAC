/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.process.controller.module;

import smartmail.logging.module.SecureTermMonitor;
import smartmail.datamodel.BodyWord;
import smartmail.datamodel.EmailAddress;
import smartmail.datamodel.MessageWord;
import smartmail.datamodel.SecureEmailAddress;
import smartmail.logging.module.ObjSerializer;
import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

/**
 *
 * @author burkep
 */
public class EmailSessionReducer extends MapReduceBase
        implements Reducer<Text, BytesWritable, Text, String> {

    @Override
    public void reduce(Text key, Iterator<BytesWritable> itrtr, OutputCollector<Text, String> oc, Reporter rprtr) throws IOException {

        int cnt = 0;
        String term = null;

        boolean issecure = false;
        boolean isbodyword = false;


        while (itrtr.hasNext()) {

            //STAC: Loop over each value associated with a key
            BytesWritable next = itrtr.next();
            MessageWord deobj = (MessageWord) ObjSerializer.deSerializeObj(next);

            if (deobj instanceof EmailAddress) {
                //System.out.println("EmailAddress:" + deobj.getValue());
                term = key.toString();
            }
            if (deobj instanceof SecureEmailAddress) {
                //System.out.println("SecureEmailAddress:" + deobj.getValue());
                SecureEmailAddress sea = (SecureEmailAddress) deobj;
                issecure = true;
            }
            if (deobj instanceof BodyWord) {
                //System.out.println("BodyWord:" + deobj.getValue());
                isbodyword = true;
            }
            //STAC: Keep count of each occurence
            cnt++;
        }

        if (term != null) {

            //STAC: If the secret address appears in the body, send out to error log, acts as an oracle
            if (isbodyword && issecure) {
                SecureTermMonitor.doubleEntryError();

            }
            oc.collect(key, cnt + ":" + issecure);
        }
    }
}
