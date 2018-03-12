package smartmail.messaging.controller.module;

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
public class RunSmartMail {

    //STAC: ENTRY POINT
    public static void main(String[] args) throws Exception {
        //Clean everything on startup
        clean("./mail");
        clean("./logs");
        RunServer.main(args);
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
