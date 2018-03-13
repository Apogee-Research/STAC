/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.process.controller.module;

import smartmail.datamodel.EmailEvent;
import smartmail.process.controller.module.seqfile.EmailParseException;
import smartmail.process.controller.module.seqfile.SequenceFileWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static smartmail.email.manager.module.parser.email.xtra.EmailParserUtil.parseemails;
import org.apache.hadoop.io.BytesWritable;
import smartmail.logging.module.LogGenerator;

/**
 *
 * @author user
 */
public class PipelineController {

    public static void main(String[] args) throws IOException, EmailParseException {

        File f = new File("mail");
        //STAC: Parse out that email amd load it into an EmailEvent object
        //STAC: This calls an actual Mime parser with callbacks
        List<EmailEvent> emails = parseemails(f);

        //STAC: Init our mapper and reducer
        EmailEventsMapper mapper = new EmailEventsMapper();
        EmailSessionReducer reducer = new EmailSessionReducer();

        //STAC: Need another one of these StateHolder objects to temporarily hold the list of words/addresses from the email before partitioning that same list
        StateHolder prepartitionstate = new StateHolder();
        //STAC: Write out list of all words addresses in email to an in-memory sequence file
        SequenceFileWriter.writeEmail(prepartitionstate, emails, null);

        //STAC: We are partition this list
        int partitionsize = 5;

        //STAC: Partition the list
        Partitioner epartition = new Partitioner(partitionsize, prepartitionstate);

        //STAC: This object holds the master state for the mapper and reducer -- holds the results
        StateHolder masterstate = new StateHolder();
        masterstate.setMapper(mapper);
        masterstate.setReducer(reducer);
        //STAC: Loop over partitions
        for (int i = 0; i < epartition.sfmaplist.size(); i++) {
            List<BytesWritable> wordspart = epartition.getPartition(i);

            //STAC: Write this partition to  the master state
            SequenceFileWriter.writeWord(masterstate, wordspart, null);

            try {
                //STAC: Map over this partition
                masterstate.callMapper();
                //STAC: Delete this partition from  the master state
                masterstate.sfclear();
            } catch (IOException ex) {
                Logger.getLogger(PipelineController.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-5);
            }
        }

        try {
            //STAC: Reduce over everything, reducer also places message in the mailbox, if one exists
            masterstate.callReducer();
        } catch (IOException ex) {
            Logger.getLogger(PipelineController.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-6);
        }

        //STAC: Log and send out everything -- Vulnerability/Observable happens here
        LogGenerator.checkValuesandOutput(masterstate.getOutput(), emails);

    }

}
