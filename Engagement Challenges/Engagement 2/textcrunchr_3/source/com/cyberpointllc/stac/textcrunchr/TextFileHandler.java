package com.cyberpointllc.stac.textcrunchr;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextFileHandler {

    List<Processor> processors;

    public TextFileHandler() throws IOException {
        // todo - fill processors with list of processors
        processors = new  ArrayList<Processor>();
        processors.add(new  CharacterCountProcessor());
        processors.add(new  TextMeterProcessor());
        processors.add(new  EnigmaProcessor());
        // Disabling SentenceStatsProcessor since there's a vulnerability in opennlp which is out
        // of scope for us at the moment. Leaving it commented in here because we might want
        // to bring it back someday.
        //processors.add(new SentenceStatsProcessor());
        processors.add(new  WordStatsProcessor());
        processors.add(new  WordFrequencyProcessor());
    }

    public void processFile(String filename, OutputHandler outph, String[] args) throws IOException {
        processFileHelper(outph, args, filename);
    }

    private void processFileHelper(OutputHandler outph, String[] args, String filename) throws IOException {
        List<String> argsList = new  ArrayList<String>(Arrays.asList(args));
        for (Processor processor : processors) {
            if (argsList.isEmpty() || argsList.contains(processor.getName())) {
                TCResult tcr = processor.process(new  FileInputStream(filename));
                outph.addResult(filename, tcr);
            }
        }
    }
}
