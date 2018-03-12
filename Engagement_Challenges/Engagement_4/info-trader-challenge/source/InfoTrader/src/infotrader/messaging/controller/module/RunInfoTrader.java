package infotrader.messaging.controller.module;

import infotrader.userinteraction.SitemapServlet;
import java.io.File;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author user
 */
public class RunInfoTrader {

    //STAC:ENTRY POINT
    public static void main(String[] args) throws Exception {
        //Clean everything on startup

        File dirs = new File("./dirs");
        if (!dirs.exists()){
            System.out.println("The dirs file does not exist. Did you not untar the dirs.tar file?");
            System.exit(-1);
        }
        clean("./reports");
        SitemapServlet.mainx(args);
    }

    public static void clean(String dir) {

        File dirf = new File(dir);
        dirf.mkdir();
        String[] entries = dirf.list();
        for (String s : entries) {
            File currentFile = new File(dirf.getPath(), s);
            currentFile.delete();
        }
    }

}
