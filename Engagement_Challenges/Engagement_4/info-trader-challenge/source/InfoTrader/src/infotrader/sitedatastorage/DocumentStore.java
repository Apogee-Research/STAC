/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infotrader.sitedatastorage;

import infotrader.dataprocessing.DocumentDisplayGenerator;
import infotrader.userinteraction.DocumentParser;
import infotrader.parser.exception.InfoTraderParserException;
import infotrader.parser.exception.InfoTraderWriterException;
import infotrader.parser.exception.WriterCancelledException;
import infotrader.parser.model.InfoTrader;
import infotrader.parser.model.Source;
import infotrader.parser.parser.InfoTraderParser;
import infotrader.parser.writer.InfoTraderWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author user
 */
public class DocumentStore {
    
    Map<String, File> files;
    
    
    public DocumentStore(){
        
        files = new HashMap<String, File>();
    
    }
    
    public void loadDoc(HttpServletResponse response, String name, Map<String, FileLink> links,  boolean recurse, DocumentDisplayGenerator display) throws IOException, InfoTraderParserException, InfoTraderWriterException {
        File readFile = getFile(name);

        InfoTraderParser gp = new InfoTraderParser();
        gp.load(readFile);

        Map<String, Source> sources = gp.infoTrader.getSources();
        Collection<Source> sourcesvalues = sources.values();
        Iterator<Source> its = sourcesvalues.iterator();

        //System.out.println("loaddoc:" + name);
        //STAC:Uncomment next line to avoid appending file we have already seen -- may be necessary to avoid HEAP vulnerability?
        //Uncommenting this line does not stop program from continuing to process the files links and keep looping
        //if (!links.containsKey(name)) {
        try (BufferedReader br = new BufferedReader(new FileReader(readFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line. Append it to the resulting output
                //System.out.println("line:" + line);
                display.appendln(line);
                
            }
            display.end();
        }
        //}

        FileLink fileLink = new FileLink(name);
        links.put(name, fileLink);
        while (its.hasNext()) {
            Source next = its.next();

            String get = next.getTitle().get(0);
            //System.out.println("t:" + get);
            fileLink.addLink(get);

            //STAC:Follow the links. If a cycle is added, his will result in eventual stack overflow exception.
            //The exception is handled gracefully.
            if (recurse) {
                loadDoc(response, get, links, recurse, display);
            }
        }
    }
    
    private static String toFileLoc(String file) throws IOException {

        String encode = URLEncoder.encode(file.toString(), "UTF-8");

        File file1 = new File("reports/" + encode);
        return file1.getAbsolutePath();
    }

    public void loadSitemap(DocumentDisplayGenerator display) throws FileNotFoundException, IOException, InfoTraderWriterException {
            try (BufferedReader br = new BufferedReader(new FileReader("Sitemap.xml"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // process the line. Append it to the resulting output
                    //System.out.println("line:" + line);
                    display.appendln(line);
                    
                }
                display.end();
            }
            return;
    }

    public void writeDoc(InfoTrader g, String timeString) throws WriterCancelledException, UnsupportedEncodingException, IOException, InfoTraderWriterException {
        InfoTraderWriter gwriter = new InfoTraderWriter(g);
        String encode = URLEncoder.encode(g.getHeader().getSourceSystem().getProductName().toString(), "UTF-8");
        File newdoc = new File("reports/" + timeString + encode);
        gwriter.write(newdoc);
        registerFile(g.getHeader().getSourceSystem().getProductName().toString(), newdoc);
    }

    public void registerFile(String name, File next) {

        files.put(name, next);
    }
    
    public File getFile(String name) {

        return files.get(name);
    }
    
     public class FileLink {

        String name;
        List<String> links;

        public FileLink(String name) {
            this.name = name;
            links = new ArrayList<String>();

        }

        public void addLink(String link) {
            links.add(link);
        }

    }
}
