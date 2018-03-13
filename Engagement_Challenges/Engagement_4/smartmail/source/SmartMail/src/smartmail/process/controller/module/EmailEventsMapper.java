/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.process.controller.module;

import smartmail.logging.module.ObjSerializer;

import java.io.IOException;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import smartmail.datamodel.MessageWord;


public class EmailEventsMapper extends MapReduceBase
        implements Mapper<Writable, BytesWritable, Text, BytesWritable> {


    public EmailEventsMapper() {
    }

    public EmailEventsMapper getnewInstance() {
        return new EmailEventsMapper();
    }

    @Override
    public void map(Writable key, BytesWritable value, OutputCollector<Text, BytesWritable> output, Reporter reporter) throws IOException {

        MessageWord deobj = (MessageWord) ObjSerializer.deSerializeObj(value);
        //STAC: Map objects  with same key to one place for Reducing/aggregation
        //STAC: This is one key for each unique email address and word in the body that is followed by a space char
        output.collect(new Text(deobj.getValue()), value);
    }
}
