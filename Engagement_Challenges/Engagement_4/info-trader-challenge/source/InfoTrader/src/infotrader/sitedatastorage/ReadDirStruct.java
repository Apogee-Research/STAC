/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infotrader.sitedatastorage;

import infotrader.dataprocessing.SiteMapGenerator;
import infotrader.parser.exception.InfoTraderParserException;
import infotrader.parser.model.Header;
import infotrader.parser.model.SourceSystem;
import infotrader.parser.model.StringWithCustomTags;
import infotrader.parser.parser.InfoTraderParser;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class ReadDirStruct {

    public static Map<String, String> restricteddir;

    /*public static void main(String args[]) throws IOException, InfoTraderParserException {
     SiteMapGenerator te = new SiteMapGenerator();
        
     load(te);
     }*/
    public static void load(DocumentStore te) {

        {
            restricteddir = new HashMap<String, String>();
            //displayIt(new File("dirs"));
            List<File> subdirs = getSubdirs(new File("dirs"));
            Iterator<File> it = subdirs.iterator();
            while (it.hasNext()) {
                File next = it.next();
                System.out.println(next.getAbsoluteFile());
                System.out.println(next.getName());
                System.out.println(next.getParent());
                System.out.println("dir:"+next.isDirectory());
                String parent = next.getParent();

                if (parent.startsWith("dirs/")) {
                    parent = parent.substring("dirs/".length());
                    //System.out.println("parent:"+parent);
                }
                if (next.getParent().equalsIgnoreCase("dirs")) {
                    parent = null;
                    //System.out.println("parent:null");
                    restricteddir.put(next.getName(), next.getName());
                }

                SiteMapGenerator.createCache(next.getName(), "Directory", parent);

                //System.out.println("------");
            }
        }
        {
            List<File> deepSubfiles = new ArrayList<File>();
            List<File> subdirs = getFnames(new File("dirs"), deepSubfiles);
            Iterator<File> it = subdirs.iterator();
            while (it.hasNext()) {
                try {
                    File next = it.next();
                    System.out.println(next.getAbsoluteFile());
                    System.out.println(next.getName());
                    System.out.println("dir:"+next.isDirectory());

                    InfoTraderParser gp = new InfoTraderParser();
                    gp.load(next);

                    Header header = gp.infoTrader.getHeader();
                    SourceSystem data = header.getSourceSystem();
                    StringWithCustomTags productName = data.getProductName();
                    String systemId = data.getSystemId();
                    //System.out.println("Name:" + productName);
                    //System.out.println("loc:" + systemId);

                    te.registerFile(productName.toString(), next);
                    SiteMapGenerator.createCache(productName.toString(), "Document", systemId);
                    // InfoTraderSenderReceiver.FileLink fileLink = new InfoTraderSenderReceiver.FileLink(name);
                } catch (IOException ex) {
                    Logger.getLogger(ReadDirStruct.class.getName()).log(Level.SEVERE, null, ex.getMessage());
                } catch (InfoTraderParserException ex) {
                    Logger.getLogger(ReadDirStruct.class.getName()).log(Level.SEVERE, null, ex.getMessage());
                }
            }

        }

    }

    static List<File> getSubdirs(File file) {
        List<File> subdirs = Arrays.asList(file.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
        }));
        subdirs = new ArrayList<File>(subdirs);

        List<File> deepSubdirs = new ArrayList<File>();
        for (File subdir : subdirs) {
            deepSubdirs.addAll(getSubdirs(subdir));
        }
        subdirs.addAll(deepSubdirs);
        return subdirs;
    }

    static List<File> getFils(File file) {
        List<File> subfils = Arrays.asList(file.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.getName().endsWith(".ged");
            }
        }));
        subfils = new ArrayList<File>(subfils);

        List<File> deepSubdirs = new ArrayList<File>();
        for (File subdir : subfils) {
            deepSubdirs.addAll(getSubdirs(subdir));
        }
        subfils.addAll(deepSubdirs);
        return subfils;
    }

    public static List<File> getFnames(File sDir, List<File> deepSubfiles) {
        //List<File> deepSubfiles = new ArrayList<File>();
        File[] faFiles = sDir.listFiles();
        for (File file : faFiles) {

            if (file.getName().endsWith(".ged")) {
        //System.out.print(".");

                deepSubfiles.add(file);
            }
            if (file.isDirectory()) {
                getFnames(file, deepSubfiles);
            }
        }
        return deepSubfiles;
    }

    public static void displayIt(File node) {

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                displayIt(new File(node, filename));
            }
        }

    }
}
