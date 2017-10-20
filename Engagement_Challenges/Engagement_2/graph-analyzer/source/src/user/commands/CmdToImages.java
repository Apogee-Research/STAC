/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user.commands;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tigris.gef.base.CmdSavePS;
import org.tigris.gef.base.Globals;

/**
 *
 * @author user
 */
public class CmdToImages implements Cmd {

    List<Exception> exs;

    public CmdToImages() {

        exs = new ArrayList<Exception>();
    }

    @Override
    public List<Exception> runCmd(Map inputmap) {

        FileOutputStream fos = null;
        try {
            String iname = (String) inputmap.get("imagename");
            fos = new FileOutputStream(iname);
            CmdSavePS sps = new CmdSavePS();
            sps.setStream(fos);
            Globals.curEditor().executeCmd(sps, null);
            return null;
        } catch (FileNotFoundException ex) {

        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(CmdToImages.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return exs;
    }

}
